package com.pratik.iiits.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.Models.GroupRequest
import com.pratik.iiits.R

class GroupRequestsAdapter(
    private val requestsList: List<GroupRequest>,
    private val approveRequest: (GroupRequest) -> Unit
) : RecyclerView.Adapter<GroupRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.user_name_text_view)
        val groupNameTextView: TextView = view.findViewById(R.id.group_name_text_view)
        val approveButton: Button = view.findViewById(R.id.approve_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestsList[position]
        holder.userNameTextView.text = request.userName
        holder.groupNameTextView.text = request.groupName
        holder.approveButton.setOnClickListener {
            approveRequest(request)
        }
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }
}
