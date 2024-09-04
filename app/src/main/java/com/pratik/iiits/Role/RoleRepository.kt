package com.pratik.iiits.Role

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class RoleRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun getRoles(): List<RoleRequest> {
        return try {
            val snapshot = firestore.collection("roleRequests").get().await()
            snapshot.toObjects(RoleRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRolesByUserId(): List<RoleRequest> {
        val currentUserUid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("roleRequests")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("status", "approved")
                .get()
                .await()
            snapshot.toObjects(RoleRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
