package com.pratik.iiits.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.pratik.iiits.Models.Group
import com.pratik.iiits.R
import kotlin.random.Random

class GroupsAdapter(
    private val groupsList: List<Group>,
    private val onItemClick: (Group) -> Unit,
    private val onItemLongClick: (Group) -> Unit,
    private val isYourGroupsList: Boolean
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    private val drawableImages = arrayOf(
        R.drawable.img_3,
        R.drawable.img_4,
        R.drawable.img_5,
        R.drawable.img_6,
        R.drawable.img_7,
        R.drawable.img_8,
        R.drawable.img_9,
        R.drawable.img_10,
        R.drawable.img_11 // Add as many images as you need
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupsList[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int = groupsList.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupProfileImage: ImageView = itemView.findViewById(R.id.group_profile_image)
        private val groupName: TextView = itemView.findViewById(R.id.group_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        private val unreadIndicator: View = itemView.findViewById(R.id.unread_indicator)

        fun bind(group: Group) {
            groupName.text = group.name

            // Check if the group is part of your groups list
            if (isYourGroupsList) {
                lastMessage.visibility = View.VISIBLE
                lastMessage.text = group.lastMessage
            } else {
                lastMessage.visibility = View.GONE
            }

            // Load group profile image
            if (!group.groupImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(group.groupImageUrl) // Use the specific image URL
                    .placeholder(R.drawable.placeholder_image) // Placeholder image
                    .into(groupProfileImage)
            } else {
                val randomImage = drawableImages[Random.nextInt(drawableImages.size)]
                Glide.with(itemView.context)
                    .load(randomImage) // Load a random image
                    .placeholder(R.drawable.placeholder_image) // Placeholder image
                    .into(groupProfileImage)
            }

            // Check for unread messages
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && group.unreadMessages[currentUser.uid] == true) {
                unreadIndicator.visibility = View.GONE
            } else {
                unreadIndicator.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(group)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(group)
                true
            }
        }
    }
}
