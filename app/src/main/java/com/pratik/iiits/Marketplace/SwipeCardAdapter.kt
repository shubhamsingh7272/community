package com.pratik.iiits.Marketplace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.pratik.iiits.R

class SwipeCardAdapter(private val context: Context, private val items: List<Item>) : BaseAdapter() {
    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        val item = items[position]

        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemDescription: TextView = view.findViewById(R.id.item_description)
        val itemPrice: TextView = view.findViewById(R.id.item_price)

        itemTitle.text = item.title
        itemDescription.text = item.description
        itemPrice.text = "₹${item.price}"

        Glide.with(context)
            .load(item.imageUrls.firstOrNull())
            .placeholder(R.drawable.placeholder_image)
            .into(itemImage)

        return view
    }
}
