package com.example.foodgal.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val file = File(context.filesDir, "products/$fileName.jpg")
            file.parentFile?.mkdirs() // create /products folder if not exists

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath // return the path to store in Firestore
        } catch (e: Exception) {
            Log.e("ImageHelper", "Failed to save image", e)
            null
        }
    }

    fun deleteImage(imagePath: String) {
        try {
            File(imagePath).delete()
        } catch (e: Exception) {
            Log.e("ImageHelper", "Failed to delete image", e)
        }
    }

}