package com.example.foodgal.ui.pos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgal.data.ProductRepository
import com.example.foodgal.models.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow(listOf("Semua", "Minuman", "Makanan", "Snack"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val db = Firebase.firestore
    private val repository = ProductRepository()

    // Logika Filter: Menggabungkan list produk dan kategori yang dipilih
    val filteredProducts: StateFlow<List<Product>> =
        combine(_products, _selectedCategory) { products, category ->
            if (category == "Semua") {
                products
            } else {
                products.filter { it.category.equals(category, ignoreCase = true) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        getProducts()
    }

    private fun getProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getProductsFlow().collect { productList ->
                _products.value = productList
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(
        context: Context,
        name: String,
        price: Double,
        category: String,
        imageUri: Uri? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.addProductWithImage(context, name, price, category, imageUri)
                .fold(
                    onSuccess = { _isLoading.value = false },
                    onFailure = { e ->
                        Log.e("ProductViewModel", "Error adding product", e)
                        _isLoading.value = false
                    }
                )
        }
    }

    fun editProduct(
        context: Context,
        productId: String,
        name: String,
        price: Double,
        category: String,
        newImageUri: Uri? = null,
        oldImagePath: String = ""
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.updateProduct(
                context,
                productId,
                name,
                price,
                category,
                newImageUri,
                oldImagePath
            )
                .fold(
                    onSuccess = {
                        _isLoading.value = false
                        getProducts()
                    },
                    onFailure = { e ->
                        Log.e("ProductViewModel", "Error editing product", e)
                        _isLoading.value = false
                    }
                )
        }
    }

    fun deleteProduct(productId: String) {
        _isLoading.value = true
        val product = _products.value.find { it.id == productId }
        viewModelScope.launch {

            repository.deleteProduct(productId, product?.imagePath ?: "")
                .fold(
                    onSuccess = {
                        _isLoading.value = false
                    },
                    onFailure = { e ->

                        Log.e("ProductViewModel", "Error deleting product", e)
                        _isLoading.value = false
                    }
                )

        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}