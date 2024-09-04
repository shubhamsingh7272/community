package com.pratik.iiits

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pratik.iiits.Adapters.UserListAdapter
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.databinding.ActivityGroupsBinding

class GroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("users")

        fetchUsersAndGroupByPost()
    }

    private fun fetchUsersAndGroupByPost() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupsMap = HashMap<String, MutableList<UserModel>>()
                for (datasnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(UserModel::class.java)
                    if (user != null && user.uid != auth.uid) {
                        val postinIIIT = user.postinIIIT
                        if (!postinIIIT.isNullOrEmpty()) { // Check for null or empty value
                            if (!groupsMap.containsKey(postinIIIT)) {
                                groupsMap[postinIIIT] = mutableListOf()
                            }
                            groupsMap[postinIIIT]?.add(user)
                        }
                    }
                }
                displayGroups(groupsMap)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun displayGroups(groupsMap: HashMap<String, MutableList<UserModel>>) {
        binding.groupsContainer.removeAllViews()
        for ((postinIIIT, userList) in groupsMap) {
            val groupView = LayoutInflater.from(this).inflate(R.layout.group_item, binding.groupsContainer, false)
            val groupIcon = groupView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.groupIcon)
            val groupTitle = groupView.findViewById<TextView>(R.id.groupTitle)

            // Set the group icon if necessary
            // groupIcon.setImageResource(R.drawable.your_group_icon)

            groupTitle.text = postinIIIT
            groupIcon.setImageResource(R.drawable.img_2)

            // Set an OnClickListener to open GroupChatActivity
            groupView.setOnClickListener {
                val intent = Intent(this, GroupChatActivity::class.java)
                intent.putExtra("GROUP_ID", postinIIIT) // Assuming postinIIIT can be used as the group ID
                startActivity(intent)
            }

            binding.groupsContainer.addView(groupView)
        }
    }
}
