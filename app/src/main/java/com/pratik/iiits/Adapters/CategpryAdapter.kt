package com.pratik.iiits.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.R

class CategoryAdapter(
    private val categories: List<String>,
    private val colors: List<Int>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val color = colors[position % colors.size] // Use modulus to cycle through colors
        holder.bind(category, color)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryCardView: CardView = itemView.findViewById(R.id.category_card_view)

        fun bind(category: String, color: Int) {
            categoryName.text = category
            categoryCardView.setCardBackgroundColor(color)
            itemView.setOnClickListener {
                onItemClick(category)
            }
        }
    }
}
