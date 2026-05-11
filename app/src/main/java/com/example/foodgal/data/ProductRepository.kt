package com.example.foodgal.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.foodgal.models.Product
import com.example.foodgal.utils.ImageHelper
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

                    val products = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }
                    trySend(products)
                    Log.d("ProductRepository", "Data Product: ${products}")

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

    // NEW: save image locally, then store path in Firestore
    suspend fun addProductWithImage(
        context: Context,
        name: String,
        price: Double,
        category: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val docRef = db.collection("products").document()

            // Save image to internal storage, use docRef.id as unique filename
            val imagePath = if (imageUri != null) {
                ImageHelper.saveImageToInternalStorage(
                    context = context,
                    uri = imageUri,
                    fileName = docRef.id  // unique name per product
                )
            } else {
                ""
            }

            val newProduct = Product(
                id = docRef.id,
                name = name,
                price = price,
                category = category,
                imagePath = imagePath ?: ""
            )

            docRef.set(newProduct).await()
            Log.d("ProductRepository", "Product added: $name, imagePath: $imagePath")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error adding product", e)
            Result.failure(e)
        }
    }

    // Update existing product: optionally replace image
    suspend fun updateProduct(
        context: Context,
        productId: String,
        name: String,
        price: Double,
        category: String,
        newImageUri: Uri?,
        oldImagePath: String
    ): Result<Unit> {
        return try {
            val imagePath = if (newImageUri != null) {
                // Delete old image first, then save new one
                if (oldImagePath.isNotEmpty()) ImageHelper.deleteImage(oldImagePath)
                ImageHelper.saveImageToInternalStorage(
                    context = context,
                    uri = newImageUri,
                    fileName = productId
                ) ?: ""
            } else {
                oldImagePath // keep existing image
            }

            val updates = mapOf(
                "name" to name,
                "price" to price,
                "category" to category,
                "imagePath" to imagePath
            )

            db.collection("products").document(productId).update(updates).await()
            Log.d("ProductRepository", "Product updated: $productId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating product", e)
            Result.failure(e)
        }
    }

    // NEW: delete image from local storage when product is deleted
    suspend fun deleteProduct(productId: String, imagePath: String): Result<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            if (imagePath.isNotEmpty()) ImageHelper.deleteImage(imagePath)
            Log.d("ProductRepository", "Product deleted: $productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error deleting product", e)
            Result.failure(e)
        }
    }

}