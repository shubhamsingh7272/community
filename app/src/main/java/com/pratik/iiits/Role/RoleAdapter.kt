package com.pratik.iiits.Role

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.R
import kotlin.random.Random

class RoleAdapter(private val context: Context, private val roleList: List<RoleRequest>) {

    fun addViewsToFlexboxLayout(flexboxLayout: ViewGroup) {
        val inflater = LayoutInflater.from(context)
        for (role in roleList) {
            val itemView = inflater.inflate(R.layout.item_role, flexboxLayout, false)
            val roleNameTextView: TextView = itemView.findViewById(R.id.role_name_text_view)
            val roleDot: View = itemView.findViewById(R.id.role_dot)
            roleNameTextView.text = role.roleName.split(" > ").last()
            roleDot.setBackgroundColor(getRandomColor(context))
            flexboxLayout.addView(itemView)
        }
    }

    private fun getRandomColor(context: Context): Int {
        val colors = context.resources.getIntArray(R.array.role_colors)
        return colors[Random.nextInt(colors.size)]
    }
}
