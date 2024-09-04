package com.pratik.iiits

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.pratik.iiits.Adapters.GroupsAdapter
import com.pratik.iiits.Models.Group
import com.pratik.iiits.Role.groupRoleManagementActivity

class GroupsListActivity : AppCompatActivity() {
    private lateinit var createGroupButton: Button
    private lateinit var yourGroupsRecyclerView: RecyclerView
    private lateinit var availableGroupsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var yourGroupsAdapter: GroupsAdapter
    private lateinit var availableGroupsAdapter: GroupsAdapter
    private val yourGroupsList = ArrayList<Group>()
    private val availableGroupsList = ArrayList<Group>()
    private var groupsListener: ListenerRegistration? = null
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups_list)

        createGroupButton = findViewById(R.id.create_group_button)
        yourGroupsRecyclerView = findViewById(R.id.your_groups_recycler_view)
        availableGroupsRecyclerView = findViewById(R.id.available_groups_recycler_view)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        yourGroupsAdapter = GroupsAdapter(yourGroupsList, ::onGroupItemClick, ::onGroupItemLongClick, true)
        availableGroupsAdapter = GroupsAdapter(availableGroupsList, ::onGroupItemClick, {}, false)
        yourGroupsRecyclerView.layoutManager = LinearLayoutManager(this)
        availableGroupsRecyclerView.layoutManager = LinearLayoutManager(this)
        yourGroupsRecyclerView.adapter = yourGroupsAdapter
        availableGroupsRecyclerView.adapter = availableGroupsAdapter

        category = intent.getStringExtra("category")
        findViewById<TextView>(R.id.categoryName).text = category.toString()
        Log.d(TAG, "Category received: $category")

        createGroupButton.setOnClickListener {
            val intent = Intent(this, GroupCreateActivity::class.java)
            intent.putExtra("CATEGORY", category)
            startActivity(intent)
        }

        checkIfAdmin()
        listenForGroupChanges()
    }

    private fun onGroupItemLongClick(group: Group) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val postInIIIT = document.getString("postinIIIT")
                        if (postInIIIT == "Admin" || postInIIIT == "Council") {
                            showDeleteConfirmationDialog(group)
                        } else {
                            Toast.makeText(this, "Only admins can delete groups", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun showDeleteConfirmationDialog(group: Group) {
        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete ${group.name}?")
            .setPositiveButton("Yes") { _, _ -> deleteGroup(group) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteGroup(group: Group) {
        firestore.collection("groups").document(group.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${group.name} deleted", Toast.LENGTH_SHORT).show()
                yourGroupsList.remove(group)
                yourGroupsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting group", e)
                Toast.makeText(this, "Failed to delete ${group.name}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfAdmin() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val postInIIIT = document.getString("postinIIIT")
                        Log.e(TAG, postInIIIT.toString())
                        if (postInIIIT == "Admin") {
                            createGroupButton.visibility = Button.VISIBLE
                            findViewById<TextView>(R.id.textview).visibility = TextView.GONE
                        } else {
                            createGroupButton.visibility = Button.GONE
                            findViewById<TextView>(R.id.textview).visibility = TextView.VISIBLE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching user details: $e")
                    createGroupButton.visibility = Button.GONE
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupsListener?.remove()
    }

    private fun listenForGroupChanges() {
        val currentUser = auth.currentUser
        if (currentUser != null && category != null) {
            groupsListener = firestore.collection("groups")
                .whereEqualTo("category", category)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed: $e")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        yourGroupsList.clear()
                        availableGroupsList.clear()

                        for (document in snapshots.documents) {
                            val group = document.toObject(Group::class.java)
                            if (group != null) {
                                firestore.collection("groups").document(group.id)
                                    .collection("messages").orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { messageSnapshot ->
                                        if (!messageSnapshot.isEmpty) {
                                            val lastMessageDoc = messageSnapshot.documents[0]
                                            val lastMessage = lastMessageDoc.getString("message") ?: ""
                                            val truncatedLastMessage = lastMessage.split("\n")[0]
                                            group.lastMessage = truncatedLastMessage
                                            group.lastMessageTime = lastMessageDoc.getLong("timestamp") ?: 0L
                                        }

                                        group.unreadMessages[currentUser.uid] = group.lastMessageTime > (document.getLong("lastReadTime_${currentUser.uid}") ?: 0L)

                                        if (group.members.contains(currentUser.uid)) {
                                            yourGroupsList.add(group)
                                        } else {
                                            availableGroupsList.add(group)
                                        }

                                        yourGroupsAdapter.notifyDataSetChanged()
                                        availableGroupsAdapter.notifyDataSetChanged()
                                    }
                            }
                        }

                        Log.d(TAG, "Your groups: ${yourGroupsList.size}")
                        Log.d(TAG, "Available groups: ${availableGroupsList.size}")

                        yourGroupsAdapter.notifyDataSetChanged()
                        availableGroupsAdapter.notifyDataSetChanged()
                    } else {
                        Log.d(TAG, "No groups found")
                    }
                }
        }
    }

    private fun onGroupItemClick(group: Group) {
        if (yourGroupsList.contains(group)) {
            openGroupChat(group)
        } else {
            showRequestDialog(group)
        }
    }

    private fun openGroupChat(group: Group) {
        val intent = Intent(this, GroupChat::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }

    private fun showRequestDialog(group: Group) {
        val dialogBuilder = BottomSheetDialog(this, R.style.BottomSheetStyle)
        dialogBuilder.setContentView(R.layout.request_dialog)
        dialogBuilder.show()
        val yes = dialogBuilder.findViewById<TextView>(R.id.yesbtn)
        val no = dialogBuilder.findViewById<TextView>(R.id.nobtn)
        val requestTitle = dialogBuilder.findViewById<TextView>(R.id.requestTitle)
        requestTitle?.text = "Do you want to send a request to join ${group.name}?"
        yes?.setOnClickListener {
            requestToJoinGroup(group)
            val intent = Intent(this, groupRoleManagementActivity::class.java)
            intent.putExtra("selectedGroup", category)
            intent.putExtra("selectedSubgroup", group.name)
            startActivity(intent)
            dialogBuilder.dismiss()
        }
        no?.setOnClickListener {
            dialogBuilder.dismiss()
        }
    }

    private fun requestToJoinGroup(group: Group) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userName = currentUser.displayName ?: "Unknown"

            val groupRequest = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "groupId" to group.id,
                "groupName" to group.name,
                "status" to "pending"
            )

            firestore.collection("groupRequests")
                .add(groupRequest)
                .addOnSuccessListener {
                    Toast.makeText(this, "Request sent to join ${group.name}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
