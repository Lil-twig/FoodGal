package com.example.foodgal.ui.pos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        // Dummy Data
        _products.value = listOf(
            Product(1, "Es Teh Manis", 5000.0, "Minuman"),
            Product(2, "Kopi Susu", 10000.0, "Minuman"),
            Product(3, "Nasi Goreng", 20000.0, "Makanan"),
            Product(4, "Ayam Bakar", 25000.0, "Makanan"),
            Product(5, "Kentang Goreng", 12000.0, "Snack"),
            Product(6, "Cireng", 8000.0, "Snack")
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(name: String, price: Double, category: String) {
        val newProduct = Product(
            id = (_products.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            price = price,
            category = category
        )
        _products.update { it + newProduct }
    }

    fun deleteProduct(productId: Int) {
        _products.update { it.filterNot { product -> product.id == productId } }
    }

    fun getFilteredProducts(): List<Product> {
        val category = _selectedCategory.value
        return if (category == "Semua") {
            _products.value
        } else {
            _products.value.filter { it.category == category }
        }
    }
}
