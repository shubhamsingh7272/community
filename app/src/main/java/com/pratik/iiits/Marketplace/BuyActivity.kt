package com.pratik.iiits.Marketplace

import android.content.Intent
import com.pratik.iiits.R
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BuyActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: ArrayList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemList = ArrayList()
        itemAdapter = ItemAdapter(itemList)
        recyclerView.adapter = itemAdapter

        fetchItems()
    }

    private fun fetchUserDetails(userIds: Set<String>, callback: (HashMap<String, Pair<String, String>>) -> Unit) {
        val usersMap = HashMap<String, Pair<String, String>>()

        db.collection("items").whereIn("userId", userIds.toList()).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.getString("userId") ?: ""
                    val username = document.getString("username") ?: ""

                    // Fetch the profile picture URL from the "profiles" collection based on the user ID
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { profileDocument ->
                            val profilePictureUrl = profileDocument.getString("imageUri") ?: ""

                            // Populate the usersMap with user IDs, usernames, and profile picture URLs
                            usersMap[userId] = Pair(username, profilePictureUrl)

                            // Check if all user details are fetched and call the callback if so
                            if (usersMap.size == userIds.size) {
                                callback(usersMap)
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    fun sell(view: View) {
        startActivity(Intent(this@BuyActivity,Sell::class.java))
    }

    private fun fetchItems() {
        db.collection("items").get()
            .addOnSuccessListener { documents ->
                val items = ArrayList<Item>()
                val userIds = HashSet<String>()

                for (document in documents) {
                    val item = document.toObject(Item::class.java)
                    items.add(item)
                    userIds.add(item.userId)
                }

                // Fetch usernames and profile picture URLs
                fetchUserDetails(userIds) { usersMap ->
                    for (item in items) {
                        val (username, profilePictureUrl) = usersMap[item.userId] ?: Pair("Unknown", "")
                        item.user = username
                        item.profilePictureUrl = profilePictureUrl

                        // Fetch email from the user's document
                        db.collection("users").document(item.userId).get()
                            .addOnSuccessListener { userDoc ->
                                item.email = userDoc.getString("email") ?: "noemail@example.com"
                                itemAdapter.notifyDataSetChanged()
                            }
                    }

                    // Update itemList and notify adapter
                    itemList.clear()
                    itemList.addAll(items)
                    itemAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }
}