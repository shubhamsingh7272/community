package com.pratik.iiits.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.R

class SearchUsersAdapter(
    private var usersList: ArrayList<UserModel>,
    private val onUserClick: (UserModel) -> Unit
) : RecyclerView.Adapter<SearchUsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.user_name)
        val userEmail: TextView = view.findViewById(R.id.user_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item1, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = usersList[position]
        holder.userName.text = user.name
        holder.userEmail.text = user.email
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    fun updateUsers(newUsersList: ArrayList<UserModel>) {
        usersList = newUsersList
        notifyDataSetChanged()
    }
}
