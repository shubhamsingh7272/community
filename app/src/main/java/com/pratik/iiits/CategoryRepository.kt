package com.pratik.iiits

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val categoriesCollection = firestore.collection("categories")

    suspend fun getCategories(): List<String> {
        return try {
            val snapshot = categoriesCollection.get().await()
            snapshot.documents.map { it.getString("name").orEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addCategory(category: String) {
        val categoryDocument = hashMapOf("name" to category)
        categoriesCollection.add(categoryDocument).await()
    }

    suspend fun deleteCategory(category: String) {
        val snapshot = categoriesCollection.whereEqualTo("name", category).get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }
}
