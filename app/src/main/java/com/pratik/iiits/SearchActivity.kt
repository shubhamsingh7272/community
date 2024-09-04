package com.pratik.iiits

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Adapters.UsersAdapter
import com.pratik.iiits.Models.UserModel

class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var usersAdapter: UsersAdapter
    private val usersList = ArrayList<UserModel>()
    private lateinit var groupId: String

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search)
//
//        searchInput = findViewById(R.id.search_input)
//        searchButton = findViewById(R.id.search_button)
//        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view)
//        firestore = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        groupId = intent.getStringExtra("groupId")!!
//
//        usersAdapter = UsersAdapter(usersList, UserModel::addUserToGroup)
//        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
//        searchResultsRecyclerView.adapter = usersAdapter
//
//        searchButton.setOnClickListener {
//            val query = searchInput.text.toString().trim()
//            if (query.isNotEmpty()) {
//                searchUsers(query)
//            }
//        }
//    }
//
//    private fun searchUsers(query: String) {
//        firestore.collection("users")
//            .whereGreaterThanOrEqualTo("name", query)
//            .whereLessThanOrEqualTo("name", query + "\uf8ff")
//            .get()
//            .addOnSuccessListener { documents ->
//                usersList.clear()
//                for (document in documents) {
//                    val user = document.toObject(UserModel::class.java)
//                    usersList.add(user)
//                }
//                usersAdapter.notifyDataSetChanged()
//            }
//    }
//
//    private fun addUserToGroup(user: UserModel) {
//        val groupRef = firestore.collection("groups").document(groupId)
//        groupRef.update("members", FieldValue.arrayUnion(user.uid))
//            .addOnSuccessListener {
//                Toast.makeText(this, "${user.name} added to group", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to add user to group", Toast.LENGTH_SHORT).show()
//            }
//    }
}
