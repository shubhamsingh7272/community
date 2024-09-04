package com.pratik.iiits.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.R

class UsersAdapter(
    private var usersList: List<UserModel>,
    private val onUserCheckedChange: ((UserModel, Boolean) -> Unit)?
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val userCheckbox: CheckBox = itemView.findViewById(R.id.user_checkbox)

        fun bind(user: UserModel) {
            userName.text = user.name
            userCheckbox.setOnCheckedChangeListener(null)
            userCheckbox.isChecked = false // Reset checkbox state
            userCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onUserCheckedChange?.let { it(user, isChecked) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(usersList[position])
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    fun updateUsers(newUsers: List<UserModel>) {
        usersList = newUsers
        notifyDataSetChanged()
    }
}

