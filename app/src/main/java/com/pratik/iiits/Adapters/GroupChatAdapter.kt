package com.pratik.iiits.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.pratik.iiits.Models.MessageModel
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.R
import java.text.SimpleDateFormat
import java.util.*

class GroupChatAdapter(private val context: Context, private val messagesList: List<MessageModel>) :
    RecyclerView.Adapter<GroupChatAdapter.MessageViewHolder>() {

    private val userMap = mutableMapOf<String, UserModel?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.messageText.text = message.message
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))
        holder.messageTime.text = time

        val user = userMap[message.senderId]
        if (user != null) {
            holder.userName.text = user.name
            Glide.with(context).load(user.imageUri).into(holder.senderImage)
        } else {
            fetchUserDetails(message.senderId, holder)
        }
    }

    override fun getItemCount(): Int = messagesList.size

    private fun fetchUserDetails(userId: String, holder: MessageViewHolder) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    userMap[userId] = user
                    holder.userName.text = user.name
                    Glide.with(context).load(user.imageUri).into(holder.senderImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val senderImage: ImageView = itemView.findViewById(R.id.senderImage)
    }
}
