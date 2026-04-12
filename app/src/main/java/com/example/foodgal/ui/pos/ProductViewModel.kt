package com.example.foodgal.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
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

    private val db = Firebase.firestore

    // Store the listener so we can clean it up later
    private var snapshotListener: ListenerRegistration? = null

    init {
        getProducts()
    }

    private fun getProducts() {

        val docref = db.collection("products")
        docref.get().addOnSuccessListener { result ->
            val productList = mutableListOf<Product>()
            for (document in result) {
                val product = document.toObject(Product::class.java)
                productList.add(product)
            }
            _products.value = productList
        }



    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(name: String, price: Double, category: String) {
        val docRef = db.collection("products").document()
        val newProduct = Product(
            id = docRef.id,
            name = name,
            price = price,
            category = category
        )

        // 3. REMOVED local state update.
        // We let Firestore's snapshot listener be the "Single Source of Truth".
        // When we add it to Firestore, the listener automatically fetches the new list.
        docRef.set(newProduct)
            .addOnSuccessListener {
                Log.d("ProductViewModel", "Product added: $name")
            }
            .addOnFailureListener { e ->
                Log.e("ProductViewModel", "Error adding document", e)
            }
    }

    fun deleteProduct(productId: String) {
        // 4. Actually delete the item from Firestore!
        // Again, no local update needed. Firestore will tell the listener the item is gone.
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d("ProductViewModel", "Product deleted: $productId")
            }
            .addOnFailureListener { e ->
                Log.e("ProductViewModel", "Error deleting document", e)
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