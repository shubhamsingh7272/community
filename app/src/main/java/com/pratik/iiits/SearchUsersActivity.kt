package com.pratik.iiits

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Adapters.SearchUsersAdapter
import com.pratik.iiits.Models.UserModel

class SearchUsersActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var postDropdown: Spinner
    private lateinit var updateButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var realtimeDatabase: FirebaseDatabase
    private lateinit var usersAdapter: SearchUsersAdapter
    private val usersList = ArrayList<UserModel>()

    private var selectedUser: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_users)

        searchInput = findViewById(R.id.search_input)
        usersRecyclerView = findViewById(R.id.users_recycler_view)
        postDropdown = findViewById(R.id.post_dropdown)
        updateButton = findViewById(R.id.update_button)
        firestore = FirebaseFirestore.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance()

        // Setup Spinner
        val postOptions = arrayOf("Admin", "Council", "Student", "Club Lead")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, postOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        postDropdown.adapter = adapter

        usersAdapter = SearchUsersAdapter(usersList) { user ->
            selectedUser = user
            val position = postOptions.indexOf(user.postinIIIT)
            postDropdown.setSelection(if (position >= 0) position else 0)
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = usersAdapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        updateButton.setOnClickListener {
            updateUserPost()
        }
    }

    private fun searchUsers(queryText: String) {
        if (queryText.isNotEmpty()) {
            firestore.collection("users")
                .orderBy("name")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error getting users", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    usersList.clear()
                    for (document in snapshots!!) {
                        val user = document.toObject(UserModel::class.java)
                        usersList.add(user)
                    }
                    usersAdapter.notifyDataSetChanged()
                }
        } else {
            usersList.clear()
            usersAdapter.notifyDataSetChanged()
        }
    }

    private fun updateUserPost() {
        val newPost = postDropdown.selectedItem.toString()
        if (selectedUser != null) {
            val userId = selectedUser!!.uid

            // Update Firestore
            firestore.collection("users").document(userId)
                .update("postinIIIT", newPost)
                .addOnSuccessListener {
                    // Update Realtime Database
                    realtimeDatabase.reference.child("users").child(userId).child("postinIIIT")
                        .setValue(newPost)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()
                            selectedUser?.postinIIIT = newPost
                            usersAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update post in Realtime Database", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update post in Firestore", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show()
        }
    }
}
