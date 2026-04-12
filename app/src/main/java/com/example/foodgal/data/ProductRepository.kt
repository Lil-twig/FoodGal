package com.example.foodgal.data

import com.example.foodgal.ui.pos.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ProductRepository {

    private val db = Firebase.firestore

    fun fetchProducts(
        onSuccess: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val productList = result.mapNotNull { it.toObject(Product::class.java) }
                onSuccess(productList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
