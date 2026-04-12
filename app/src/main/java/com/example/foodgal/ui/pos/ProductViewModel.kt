package com.example.foodgal.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.foodgal.data.ProductRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val db = Firebase.firestore
    private val repository = ProductRepository()

    // Store the listener so we can clean it up later
    private var snapshotListener: ListenerRegistration? = null

    init {
        getProducts()
    }

    private fun getProducts() {
        _isLoading.value = true
        repository.fetchProducts(
            onSuccess = { productList ->
                _products.value = productList
                _isLoading.value = false
            },
            onFailure = { e ->
                Log.e("ProductViewModel", "Error fetching products", e)
                _isLoading.value = false
            }
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(name: String, price: Double, category: String) {
        _isLoading.value = true
        val docRef = db.collection("products").document()
        val newProduct = Product(
            id = docRef.id,
            name = name,
            price = price,
            category = category
        )

        docRef.set(newProduct)
            .addOnSuccessListener {
                Log.d("ProductViewModel", "Product added: $name")
                getProducts()
            }
            .addOnFailureListener { e ->
                Log.e("ProductViewModel", "Error adding document", e)
                _isLoading.value = false
            }
    }

    fun deleteProduct(productId: String) {
        _isLoading.value = true
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d("ProductViewModel", "Product deleted: $productId")
                getProducts()
            }
            .addOnFailureListener { e ->
                Log.e("ProductViewModel", "Error deleting document", e)
                _isLoading.value = false
            }
    }

    fun getFilteredProducts(): List<Product> {
        val category = _selectedCategory.value
        return if (category == "Semua") {
            _products.value
        } else {
            _products.value.filter { it.category == category }
        }
    }

    // 5. Prevent memory leaks!
    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}
