# Midtrans Payment Implementation Guide

## Overview

Current flow: `POS → Checkout → Success → Receipt`

With Midtrans, the flow becomes:
```
POS → Checkout → [Midtrans SDK WebView] → Success/Failed → Receipt
```

The Midtrans Snap SDK opens its own Activity (WebView). The payment result
comes back via `onActivityResult` in `MainActivity`.

---

## 1. Firestore `transactions` Collection Schema

```
transactions/
  {orderId}/                         // e.g. "TRX-1714200000000-uid123"
    orderId         : String
    cashierId       : String          // Firebase Auth UID
    items           : List<Map>
      - productId   : String
      - name        : String
      - price       : Double
      - quantity    : Int
      - subtotal    : Double
    totalAmount     : Double
    paymentMethod   : String          // "midtrans"
    paymentStatus   : String          // "pending" | "success" | "failed" | "cancelled"
    snapToken       : String          // token from Midtrans Snap API
    midtransOrderId : String          // same as orderId
    createdAt       : Timestamp
    updatedAt       : Timestamp
```

---

## 2. Gradle Dependencies

### `settings.gradle.kts` — add Midtrans maven repo

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.midtrans.com/artifactory/libs-release") }  // ADD THIS
    }
}
```

### `app/build.gradle.kts`

```kotlin
android {
    defaultConfig {
        // ...
        buildConfigField("String", "MIDTRANS_CLIENT_KEY", "\"${properties["MIDTRANS_CLIENT_KEY"]}\"")
        buildConfigField("String", "MIDTRANS_BASE_URL", "\"https://api.sandbox.midtrans.com/\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true   // ADD THIS to enable BuildConfig fields
    }
}

dependencies {
    // existing deps ...

    // Midtrans Snap UI SDK
    implementation("com.midtrans:uikit:2.0.0")

    // Firebase Functions (for calling Cloud Function)
    implementation("com.google.firebase:firebase-functions")
}
```

### `local.properties` — never commit this file

```
MIDTRANS_CLIENT_KEY=SB-Mid-client-xxxxxxxxxxxx
```

---

## 3. Transaction Data Class

Create file: `app/src/main/java/com/example/foodgal/ui/pos/Transaction.kt`

```kotlin
package com.example.foodgal.ui.pos

import com.google.firebase.Timestamp

data class TransactionItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val subtotal: Double = 0.0
)

data class Transaction(
    val orderId: String = "",
    val cashierId: String = "",
    val items: List<TransactionItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "midtrans",
    val paymentStatus: String = "pending",
    val snapToken: String = "",
    val midtransOrderId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

---

## 4. TransactionRepository

Create file: `app/src/main/java/com/example/foodgal/data/TransactionRepository.kt`

```kotlin
package com.example.foodgal.data

import com.example.foodgal.ui.pos.Transaction
import com.example.foodgal.ui.pos.TransactionItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    private val db = Firebase.firestore
    private val functions = Firebase.functions("asia-southeast1") // or your region

    suspend fun getSnapToken(
        orderId: String,
        amount: Int,
        items: List<TransactionItem>,
        customerName: String
    ): String {
        val payload = hashMapOf(
            "orderId" to orderId,
            "amount" to amount,
            "customerName" to customerName,
            "items" to items.map {
                mapOf(
                    "id" to it.productId,
                    "name" to it.name,
                    "price" to it.price.toInt(),
                    "quantity" to it.quantity
                )
            }
        )
        val result = functions.getHttpsCallable("createSnapToken").call(payload).await()
        @Suppress("UNCHECKED_CAST")
        return (result.data as Map<String, Any>)["token"] as String
    }

    suspend fun saveTransaction(transaction: Transaction) {
        db.collection("transactions")
            .document(transaction.orderId)
            .set(transaction)
            .await()
    }

    suspend fun updateStatus(orderId: String, status: String) {
        db.collection("transactions")
            .document(orderId)
            .update(
                "paymentStatus", status,
                "updatedAt", Timestamp.now()
            ).await()
    }
}
```

---

## 5. Firebase Cloud Function

### Setup

```bash
# In project root
npm install -g firebase-tools
firebase login
firebase init functions   # choose JavaScript, or TypeScript
cd functions
npm install axios
```

### `functions/index.js`

```js
const functions = require("firebase-functions");
const axios = require("axios");

exports.createSnapToken = functions
  .region("asia-southeast1")
  .https.onCall(async (data) => {
    const { orderId, amount, items, customerName } = data;

    const serverKey = functions.config().midtrans.server_key;
    const encoded = Buffer.from(serverKey + ":").toString("base64");

    const response = await axios.post(
      "https://app.sandbox.midtrans.com/snap/v1/transactions",
      {
        transaction_details: {
          order_id: orderId,
          gross_amount: amount,
        },
        item_details: items.map((item) => ({
          id: item.id,
          name: item.name,
          price: item.price,
          quantity: item.quantity,
        })),
        customer_details: {
          first_name: customerName,
        },
      },
      {
        headers: {
          Authorization: `Basic ${encoded}`,
          "Content-Type": "application/json",
        },
      }
    );

    return { token: response.data.token };
  });
```

### Deploy

```bash
# Set server key as environment config (never hardcode)
firebase functions:config:set midtrans.server_key="SB-Mid-server-xxxxxxxxxxxx"

firebase deploy --only functions
```

---

## 6. PosViewModel — Add Payment State

Add to `PosViewModel.kt`:

```kotlin
import com.example.foodgal.data.TransactionRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// Inside PosViewModel class:

private val transactionRepo = TransactionRepository()

// SharedFlow to trigger Midtrans SDK launch from MainActivity
private val _snapEvent = MutableSharedFlow<Pair<String, String>>() // orderId, snapToken
val snapEvent: SharedFlow<Pair<String, String>> = _snapEvent

private val _paymentError = MutableStateFlow<String?>(null)
val paymentError: StateFlow<String?> = _paymentError.asStateFlow()

private val _isPaymentLoading = MutableStateFlow(false)
val isPaymentLoading: StateFlow<Boolean> = _isPaymentLoading.asStateFlow()

// Track current orderId so MainActivity can update status after result
private var pendingOrderId: String? = null

fun startPayment(cartProducts: List<Pair<Product, Int>>, total: Int) {
    val cashierId = Firebase.auth.currentUser?.uid ?: return
    val orderId = "TRX-${System.currentTimeMillis()}-${cashierId.take(6)}"
    pendingOrderId = orderId

    viewModelScope.launch {
        _isPaymentLoading.value = true
        try {
            val txItems = cartProducts.map { (product, qty) ->
                TransactionItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = qty,
                    subtotal = product.price * qty
                )
            }

            val token = transactionRepo.getSnapToken(
                orderId = orderId,
                amount = total,
                items = txItems,
                customerName = "FoodGal Customer"
            )

            // Save pending transaction to Firestore
            transactionRepo.saveTransaction(
                Transaction(
                    orderId = orderId,
                    cashierId = cashierId,
                    items = txItems,
                    totalAmount = total.toDouble(),
                    snapToken = token,
                    midtransOrderId = orderId
                )
            )

            _snapEvent.emit(orderId to token)
        } catch (e: Exception) {
            _paymentError.value = "Gagal memulai pembayaran: ${e.message}"
        } finally {
            _isPaymentLoading.value = false
        }
    }
}

fun onPaymentResult(isSuccess: Boolean) {
    val orderId = pendingOrderId ?: return
    viewModelScope.launch {
        val status = if (isSuccess) "success" else "failed"
        transactionRepo.updateStatus(orderId, status)
        if (!isSuccess) pendingOrderId = null
    }
}

fun clearPendingOrder() {
    pendingOrderId = null
}
```

---

## 7. CheckoutScreen — Trigger Payment

Replace the "Complete Purchase" button `onClick` in `CheckoutScreen.kt`:

```kotlin
// Add to imports
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.app.Activity

@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    viewModel: PosViewModel,
    onComplete: () -> Unit    // called on payment success
) {
    val isLoading by viewModel.isPaymentLoading.collectAsState()
    val paymentError by viewModel.paymentError.collectAsState()

    // ... existing state ...

    // Replace the Button:
    Button(
        onClick = { viewModel.startPayment(checkoutItems, totalPrice) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        enabled = checkoutItems.isNotEmpty() && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Bayar Sekarang", modifier = Modifier.padding(8.dp))
        }
    }

    // Show error if any
    paymentError?.let { error ->
        Snackbar { Text(error) }
    }
}
```

---

## 8. MainActivity — Init SDK & Handle Result

Replace `MainActivity.kt`:

```kotlin
package com.example.foodgal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.foodgal.ui.navigation.NavGraph
import com.example.foodgal.ui.pos.PosViewModel
import com.example.foodgal.ui.theme.FoodGalTheme
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Shared reference so NavGraph can trigger Midtrans launch
    private lateinit var posViewModel: PosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        posViewModel = ViewModelProvider(this)[PosViewModel::class.java]

        // Init Midtrans SDK once
        UiKitApi.Builder()
            .withMerchantClientKey(BuildConfig.MIDTRANS_CLIENT_KEY)
            .withContext(this)
            .withMerchantUrl(BuildConfig.MIDTRANS_BASE_URL)
            .enableLog(true)                      // set false for production
            .withColorTheme(CustomColorTheme("#4CAF50", "#4CAF50", "#4CAF50"))
            .build()

        // Observe snap token events → launch Midtrans SDK
        lifecycleScope.launch {
            posViewModel.snapEvent.collect { (_, token) ->
                UiKitApi.getDefaultInstance().startPaymentUiFlow(
                    this@MainActivity,
                    launcher,   // see below
                    token
                )
            }
        }

        setContent {
            FoodGalTheme {
                NavGraph(posViewModel = posViewModel)
            }
        }
    }

    // Activity Result launcher for Midtrans SDK
    private val launcher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val transactionResult = result.data?.getParcelableExtra<com.midtrans.sdk.uikit.api.model.TransactionResult>(
                UiKitConstants.KEY_TRANSACTION_RESULT
            )
            val isSuccess = transactionResult?.status == "settlement" ||
                            transactionResult?.status == "capture"
            posViewModel.onPaymentResult(isSuccess)
            // NavGraph observes posViewModel state to navigate to Success or show error
        }
    }
}
```

---

## 9. NavGraph — Observe Payment Result & Navigate

In `NavGraph.kt`, pass `posViewModel` as parameter and observe the result:

```kotlin
@Composable
fun NavGraph(posViewModel: PosViewModel = viewModel()) {
    // ...existing code...

    // Observe payment success → navigate to Success screen
    val paymentError by posViewModel.paymentError.collectAsState()

    LaunchedEffect(Unit) {
        // You can use a dedicated paymentSuccess StateFlow in PosViewModel
        // to drive navigation here. Example:
        posViewModel.paymentSuccess.collect { success ->
            if (success) {
                navController.navigate(Screen.Success.route) {
                    popUpTo(Screen.Checkout.route) { inclusive = true }
                }
                posViewModel.clearPendingOrder()
            }
        }
    }
    // ...rest of NavGraph...
}
```

Add `paymentSuccess` SharedFlow to `PosViewModel`:

```kotlin
private val _paymentSuccess = MutableSharedFlow<Boolean>()
val paymentSuccess: SharedFlow<Boolean> = _paymentSuccess

// Call this inside onPaymentResult:
fun onPaymentResult(isSuccess: Boolean) {
    val orderId = pendingOrderId ?: return
    viewModelScope.launch {
        val status = if (isSuccess) "success" else "failed"
        transactionRepo.updateStatus(orderId, status)
        _paymentSuccess.emit(isSuccess)
        if (!isSuccess) pendingOrderId = null
    }
}
```

---

## 10. Firestore Security Rules

Add to `firestore.rules`:

```
match /transactions/{transactionId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null
    && request.resource.data.cashierId == request.auth.uid;
  allow update: if request.auth != null
    && resource.data.cashierId == request.auth.uid
    && request.resource.data.diff(resource.data).affectedKeys()
       .hasOnly(['paymentStatus', 'updatedAt']);
}
```

---

## Summary of Files to Create/Modify

| Action | File |
|--------|------|
| **Create** | `app/.../ui/pos/Transaction.kt` |
| **Create** | `app/.../data/TransactionRepository.kt` |
| **Create** | `functions/index.js` |
| **Modify** | `app/build.gradle.kts` — add dependency + BuildConfig fields |
| **Modify** | `settings.gradle.kts` — add Midtrans maven repo |
| **Modify** | `local.properties` — add `MIDTRANS_CLIENT_KEY` |
| **Modify** | `app/.../ui/pos/PosViewModel.kt` — add payment logic |
| **Modify** | `app/.../ui/pos/CheckoutScreen.kt` — new button behavior |
| **Modify** | `app/.../MainActivity.kt` — init SDK + activity result |
| **Modify** | `app/.../ui/navigation/NavGraph.kt` — accept posViewModel param, observe paymentSuccess |
| **Modify** | `firestore.rules` — add transactions rules |

## Sandbox vs Production

| | Sandbox | Production |
|---|---|---|
| Client key | `SB-Mid-client-...` | `Mid-client-...` |
| Server key | `SB-Mid-server-...` | `Mid-server-...` |
| Snap URL | `app.sandbox.midtrans.com` | `app.midtrans.com` |
| SDK log | `enableLog(true)` | `enableLog(false)` |

Always use sandbox during development. Switch keys + URL when going live.