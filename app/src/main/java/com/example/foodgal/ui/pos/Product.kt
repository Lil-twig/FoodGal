package com.example.foodgal.ui.pos

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val imageRes: Int? = null
)
