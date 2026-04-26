package com.example.foodgal.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgal.ui.component.formatToRupiah
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptScreen(viewModel: PosViewModel, onDoneClick: () -> Unit) {
    val cartItems by viewModel.cartItems.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()

    val receiptItems = cartItems.mapNotNull { (id, qty) ->
        allProducts.find { it.id == id }?.let { it to qty }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.padding(24.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "FOODGAL RESTO",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp
                )
                Text("Jl. Merdeka No. 123", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("--------------------------------", fontFamily = FontFamily.Monospace)

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(receiptItems) { (product, qty) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${qty}x ${product.name}",
                                modifier = Modifier.weight(1f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                formatToRupiah(product.price.toInt() * qty),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text("--------------------------------", fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(
                        formatToRupiah(totalPrice),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "TERIMA KASIH",
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )
            }
            Button(onClick = onDoneClick, modifier = Modifier.fillMaxWidth()) {
                Text("Selesai & Reset POS")
            }
        }
    }
}