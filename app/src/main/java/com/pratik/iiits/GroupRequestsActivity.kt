package com.pratik.iiits

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Adapters.GroupRequestsAdapter
import com.pratik.iiits.Models.GroupRequest

class GroupRequestsActivity : AppCompatActivity() {
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var requestsAdapter: GroupRequestsAdapter
    private val requestsList = ArrayList<GroupRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_requests)

        requestsRecyclerView = findViewById(R.id.requests_recycler_view)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        requestsAdapter = GroupRequestsAdapter(requestsList, ::approveRequest)
        requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        requestsRecyclerView.adapter = requestsAdapter

        loadRequests()
    }

    private fun loadRequests() {
        firestore.collection("groupRequests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to load requests: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    requestsList.clear()
                    for (document in snapshots.documents) {
                        val request = document.toObject(GroupRequest::class.java)
                        if (request != null) {
                            request.id = document.id // Ensure the id is set
                            requestsList.add(request)
                        }
                    }
                    requestsAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun approveRequest(request: GroupRequest) {
        val groupId = request.groupId
        val userId = request.userId
        val requestId = request.id

        // Log the requestId to ensure it is not null or empty
        Log.d("GroupRequestsActivity", "Approving request with ID: $requestId")

        if (requestId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("groups").document(groupId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                firestore.collection("groupRequests").document(requestId)
                    .update("status", "approved")
                    .addOnSuccessListener {
                        sendApprovalNotification(request)
                        Toast.makeText(this, "Request has been approved", Toast.LENGTH_SHORT).show() // Toast for approval
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update request status: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update group members: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendApprovalNotification(request: GroupRequest) {
        val userId = request.userId
        val notificationMessage = "Your request to join ${request.groupName} has been approved."

        firestore.collection("fcmTokens").document(userId).get()
            .addOnSuccessListener { document ->
                val token = document.getString("token")
                if (token != null) {
                    sendFCMNotification(token, "Request Approved", notificationMessage)
                }
            }
    }

    private fun sendFCMNotification(token: String, title: String, message: String) {
        val notificationData = mapOf(
            "to" to token,
            "notification" to mapOf(
                "title" to title,
                "body" to message
            )
        )

        FirebaseFirestore.getInstance().collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                // Notification sent
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
}
