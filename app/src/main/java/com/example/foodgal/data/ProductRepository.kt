package com.example.foodgal.data

import com.example.foodgal.ui.pos.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ProductRepository {

    private val db = Firebase.firestore

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val subscription = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    trySend(products)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun fetchProducts(
        onSuccess: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val productList = result.toObjects(Product::class.java)
                onSuccess(productList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
