package com.pratik.iiits

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pratik.iiits.Adapters.GroupChatAdapter
import com.pratik.iiits.Models.MessageModel
import com.pratik.iiits.databinding.ActivityGroupChatBinding

class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messagesReference: DatabaseReference
    private lateinit var groupId: String
    private lateinit var adapter: GroupChatAdapter
    private val messagesList = mutableListOf<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra("GROUP_ID") ?: return

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        messagesReference = database.reference.child("groups").child(groupId).child("messages")

        // Set the group name in the header
        binding.groupHeaderTitle.text = groupId

        adapter = GroupChatAdapter(this, messagesList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

//        binding.sendButton.setOnClickListener {
//            sendMessage()
//        }

        fetchMessages()
    }

    fun sendMessage(view: View) {
        val messageText = binding.messageInput.text.toString()
        if (messageText.isNotEmpty()) {
            val message = MessageModel(messageText, auth.uid ?: "", System.currentTimeMillis())
            messagesReference.push().setValue(message)
            binding.messageInput.text.clear()
        }
    }

    private fun fetchMessages() {
        messagesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (datasnapshot in snapshot.children) {
                    val message = datasnapshot.getValue(MessageModel::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(messagesList.size - 1) // Scroll to the latest message
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}
