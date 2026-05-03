package com.example.foodgal.ui.pos

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgal.data.ProductRepository
import com.example.foodgal.models.Product
import com.example.foodgal.models.Transaction
import com.example.foodgal.models.TransactionItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PosViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    // CART STATE
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap()) // productId to quantity
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getProductsFlow().collect { products ->
                _allProducts.value = products
                updateFilteredList()
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        updateFilteredList()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val currentCategory = _selectedCategory.value
        val currentQuery = _searchQuery.value.lowercase()

        _filteredProducts.value = _allProducts.value.filter { product ->
            val matchesCategory = if (currentCategory == "All") true 
                                  else product.category.equals(currentCategory, ignoreCase = true)
            val matchesSearch = if (currentQuery.isEmpty()) true 
                                else product.name.lowercase().contains(currentQuery)
            
            matchesCategory && matchesSearch
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cartItems.value.toMutableMap()
        val count = currentCart.getOrDefault(product.id, 0)
        currentCart[product.id] = count + 1
        _cartItems.value = currentCart
        
        _totalItems.value = currentCart.values.sum()
        _totalPrice.value += product.price.toInt()
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    fun removeFromCart(product: Product) {
        val currentCart = _cartItems.value.toMutableMap()
        val count = currentCart.getOrDefault(product.id, 0)
        if (count > 0) {
            if (count == 1) {
                currentCart.remove(product.id)
            } else {
                currentCart[product.id] = count - 1
            }
            _cartItems.value = currentCart
            _totalItems.value = currentCart.values.sum()
            _totalPrice.value -= product.price.toInt()
        }
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _totalItems.value = 0
        _totalPrice.value = 0
    }

    // LOGIC TO SAVE TRANSACTION TO FIREBASE
    fun completeTransaction(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("Sesi berakhir, silakan login kembali")
            return
        }

        if (_cartItems.value.isEmpty()) {
            onFailure("Keranjang masih kosong")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                // GANTI AWALAN DI SINI: dari "TRX-" menjadi "Struk Pembelian "
                val orderId = "TRX-$timestamp"
                
                val transactionItems = _cartItems.value.mapNotNull { (productId, quantity) ->
                    val product = _allProducts.value.find { it.id == productId }
                    product?.let {
                        TransactionItem(
                            productId = it.id,
                            name = it.name,
                            price = it.price,
                            quantity = quantity,
                            subtotal = it.price * quantity
                        )
                    }
                }

                val transaction = Transaction(
                    orderId = orderId,
                    cashierId = currentUser.uid,
                    items = transactionItems,
                    totalAmount = _totalPrice.value.toDouble(),
                    paymentMethod = "Cash", 
                    paymentStatus = "success",
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )

                // Simpan ke koleksi "transactions"
                db.collection("transactions")
                    .document(orderId)
                    .set(transaction)
                    .await()

                Log.d("PosViewModel", "Transaksi Berhasil: $orderId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("PosViewModel", "Gagal menyimpan transaksi", e)
                onFailure("Gagal memproses transaksi: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
