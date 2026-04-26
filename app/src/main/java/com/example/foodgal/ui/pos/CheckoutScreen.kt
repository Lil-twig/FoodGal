package com.example.foodgal.ui.pos

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodgal.R
import com.example.foodgal.ui.component.formatToRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    viewModel: PosViewModel,
    onComplete: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()

    // Map cart items to actual product objects and their quantities
    val checkoutItems = cartItems.mapNotNull { (productId, quantity) ->
        val product = products.find { it.id == productId }
        if (product != null) product to quantity else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(checkoutItems) { (product, quantity) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name, fontWeight = FontWeight.Medium)
                            Text(
                                text = "x$quantity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                    .clickable { viewModel.addToCart(product) }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(text = quantity.toString(), fontWeight = FontWeight.Bold)

                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.removeFromCart(product) }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.minus_24),
                                    contentDescription = "Kurangi",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(text = formatToRupiah(product.price.toInt() * quantity))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatToRupiah(totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onComplete() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = checkoutItems.isNotEmpty()
            ) {
                Text("Complete Purchase", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
