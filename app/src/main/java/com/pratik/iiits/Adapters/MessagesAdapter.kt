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
import com.pratik.iiits.Models.Message
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.R
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(private val context: Context, private val messagesList: List<Message>,private var groupName: String ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val userMap = mutableMapOf<String, UserModel?>()
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_TEXT = 1
    private val VIEW_TYPE_IMAGE = 2

    inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val senderImage: ImageView = itemView.findViewById(R.id.senderImage)
    }

    inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val senderImage: ImageView = itemView.findViewById(R.id.senderImage)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerMessage: TextView = itemView.findViewById(R.id.header_message)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_HEADER
            messagesList[position - 1].imageUrl != null -> VIEW_TYPE_IMAGE
            else -> VIEW_TYPE_TEXT
        }
    }
    fun setGroupName(name: String) {
        groupName = name
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_header_message, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_text_message, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_image_message, parent, false)
                ImageMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val headerHolder = holder as HeaderViewHolder
            headerHolder.headerMessage.text = "Welcome to $groupName !!!\nMessages are end-to-end encrypted. No one outside of this chat can read them."
        } else {
            val message = messagesList[position - 1] // Adjust for header

            if (holder is TextMessageViewHolder) {
                bindTextMessage(holder, message)
            } else if (holder is ImageMessageViewHolder) {
                bindImageMessage(holder, message)
            }
        }
    }

    private fun bindTextMessage(holder: TextMessageViewHolder, message: Message) {
        // Check if user details are already fetched
        if (userMap.containsKey(message.senderId)) {
            val user = userMap[message.senderId]
            holder.userName.text = user?.name ?: ""  // Use null-check operator
            Glide.with(context).load(user?.imageUri ?: "").into(holder.senderImage)
        } else {
            fetchUserDetails(message.senderId, holder)
        }

        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))
        holder.messageTime.text = time
        holder.messageText.text = message.message
    }

    private fun bindImageMessage(holder: ImageMessageViewHolder, message: Message) {
        // Check if user details are already fetched
        if (userMap.containsKey(message.senderId)) {
            val user = userMap[message.senderId]
            holder.userName.text = user?.name ?: ""  // Use null-check operator
            Glide.with(context).load(user?.imageUri ?: "").into(holder.senderImage)
        } else {
            fetchUserDetails(message.senderId, holder)
        }

        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))
        holder.messageTime.text = time
        Glide.with(holder.messageImage.context)
            .load(message.imageUrl)
            .into(holder.messageImage)
    }

    private fun fetchUserDetails(userId: String, holder: RecyclerView.ViewHolder?) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    userMap[userId] = user
                    notifyDataSetChanged()  // Notify the adapter that the data has changed
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    override fun getItemCount(): Int {
        return messagesList.size + 1  // Adjust for header
    }
}
