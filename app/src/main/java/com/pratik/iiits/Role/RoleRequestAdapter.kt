package com.pratik.iiits.Role

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R

class RoleRequestAdapter(private val onRequestAction: (String, String, String, String) -> Unit) :
    ListAdapter<RoleRequest, RoleRequestAdapter.RoleRequestViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_role_request, parent, false)
        return RoleRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoleRequestViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request, onRequestAction)
    }

    fun removeRequest(requestId: String) {
        val updatedList = currentList.filter { it.id != requestId }
        submitList(updatedList)
    }

    inner class RoleRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val groupTextView: TextView = itemView.findViewById(R.id.group_text_view)
        private val subgroupTextView: TextView = itemView.findViewById(R.id.subgroup_text_view)
        private val subsubgroupTextView: TextView = itemView.findViewById(R.id.subsubgroup_text_view)
        private val approveButton: Button = itemView.findViewById(R.id.approve_button)
        private val rejectButton: Button = itemView.findViewById(R.id.reject_button)

        fun bind(request: RoleRequest, onRequestAction: (String, String, String, String) -> Unit) {
            // Fetch the actual username from Firestore using the userId
            FirebaseFirestore.getInstance().collection("users").document(request.userId).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("name") ?: "Unknown User"
                    usernameTextView.text = username
                } else {
                    usernameTextView.text = "Unknown User"
                }
            }.addOnFailureListener {
                usernameTextView.text = "Error fetching username"
            }

            val roleParts = request.roleName.split(" > ")
            groupTextView.text = roleParts.getOrNull(0) ?: "N/A"
            subgroupTextView.text = roleParts.getOrNull(1) ?: "N/A"
            subsubgroupTextView.text = roleParts.getOrNull(2) ?: "N/A"

            approveButton.setOnClickListener {
                onRequestAction(request.id, request.userId, request.roleName, "approve")
            }
            rejectButton.setOnClickListener {
                onRequestAction(request.id, request.userId, request.roleName, "reject")
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RoleRequest>() {
        override fun areItemsTheSame(oldItem: RoleRequest, newItem: RoleRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RoleRequest, newItem: RoleRequest): Boolean {
            return oldItem == newItem
        }
    }
}
