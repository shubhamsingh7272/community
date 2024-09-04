package com.pratik.iiits

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Adapters.UsersAdapter
import com.pratik.iiits.Models.Group
import com.pratik.iiits.Models.UserModel

class GroupCreateActivity : AppCompatActivity() {
    private lateinit var groupNameEditText: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var createGroupButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var categorySpinner: Spinner

    private lateinit var usersAdapter: UsersAdapter
    private val usersList = ArrayList<UserModel>()
    private val selectedUsers = HashSet<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_create)

        groupNameEditText = findViewById(R.id.group_name_edit_text)
        usersRecyclerView = findViewById(R.id.users_recycler_view)
        createGroupButton = findViewById(R.id.create_group_button)
        categorySpinner = findViewById(R.id.category_spinner)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val adapter = ArrayAdapter.createFromResource(this, R.array.categories_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        usersAdapter = UsersAdapter(usersList, ::onUserCheckedChange)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = usersAdapter

        loadUsers()

        createGroupButton.setOnClickListener {
            createGroup()
        }
    }

    private fun loadUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                usersList.clear()
                for (document in documents) {
                    val user = document.toObject(UserModel::class.java)
                    usersList.add(user)
                }
                usersAdapter.notifyDataSetChanged()
            }
    }

    private fun onUserCheckedChange(user: UserModel, isChecked: Boolean) {
        if (isChecked) {
            selectedUsers.add(user)
        } else {
            selectedUsers.remove(user)
        }
    }

    private fun createGroup() {
        val groupName = groupNameEditText.text.toString().trim()
        val category = intent.getStringExtra("CATEGORY")
        if (groupName.isEmpty()) {
            groupNameEditText.error = "Group name is required"
            groupNameEditText.requestFocus()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val groupId = firestore.collection("groups").document().id
            val adminId = currentUser.uid
            val members = selectedUsers.map { it.uid }.toMutableList()
            members.add(adminId) // Add admin to the members list

            val group = category?.let {
                Group(
                    id = groupId,
                    name = groupName,
                    admin = adminId,
                    members = members,
                    category = it
                )
            }

            if (group != null) {
                firestore.collection("groups").document(groupId)
                    .set(group)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to create group: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
