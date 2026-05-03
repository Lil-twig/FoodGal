package com.example.foodgal.models

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
    val paymentMethod: String = "",
    val paymentStatus: String = "pending", // pending, success, failed, cancelled
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
