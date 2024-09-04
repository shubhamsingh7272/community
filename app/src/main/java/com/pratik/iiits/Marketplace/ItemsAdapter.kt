package com.pratik.iiits.Marketplace


import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide

import com.pratik.iiits.R
import java.io.Serializable






class ItemAdapter(private var itemList: MutableList<Item> = mutableListOf()
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val viewPager: ViewPager = itemView.findViewById(R.id.viewPager)
        val user: TextView = itemView.findViewById(R.id.tvUser) // Add this line
        val profile:ImageView=itemView.findViewById(R.id.ivProfilePicture)

        val contactButton: Button = itemView.findViewById(R.id.email_seller_button) // Add this line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.title.text = currentItem.title
        holder.description.text = currentItem.description
        holder.price.text = "₹${currentItem.price}"
        holder.user.text = currentItem.user // Display username
        Glide.with(holder.itemView.context)
            .load(currentItem.profilePictureUrl)
            .placeholder(R.drawable.profile) // Placeholder image
            .error(R.drawable.profile) // Error image
            .into(holder.profile)

        val adapter = ImagePagerAdapter(holder.itemView.context, currentItem.imageUrls)
        holder.viewPager.adapter = adapter

        holder.contactButton.setOnClickListener {
            val subject = "Inquiry about ${currentItem.title}"
            val body = """
                Hi ${currentItem.user},

                I am interested in your Item "${currentItem.title}" listed for ${currentItem.price}. Could you please provide more details?

                Thank you,
                [Your Name]
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${currentItem.email}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            holder.itemView.context.startActivity(intent)
        }


    }

    override fun getItemCount() = itemList.size




}