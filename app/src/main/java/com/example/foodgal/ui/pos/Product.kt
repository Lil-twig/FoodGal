package com.example.foodgal.ui.pos

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageRes: Int? = null
)
