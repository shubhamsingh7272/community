package com.pratik.iiits.notes.Adapters

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.Models.Post
import com.pratik.iiits.databinding.ItemPostsBinding
import com.pratik.iiits.notes.PostDetailDialogFragment
import com.squareup.picasso.Picasso

class PostsAdapter(
    private val context: Context,
    private val posts: MutableList<Post>, // Changed from List to MutableList for item removal
    private val userRole: String // Added userRole parameter to check if user is Admin
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    inner class ViewHolder(private val binding: ItemPostsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.name.text = post.user?.name ?: "NULL"
            binding.description.text = post.description ?: "No description"
            val placeholderImageUrl = "https://bit.ly/3T5Uk5W"
            Picasso.get().load(placeholderImageUrl).into(binding.userimage)
            binding.time.text = DateUtils.getRelativeTimeSpanString(post.creation_time_ms)
            Picasso.get().load(post.image_url).into(binding.postImage)

            // Click to open post detail
            binding.root.setOnClickListener {
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                val postDetailDialog = PostDetailDialogFragment.newInstance(post)
                postDetailDialog.show(fragmentManager, "post_detail_dialog")
            }

            // Long click to delete post if user is Admin
            binding.root.setOnLongClickListener {
                // Log the userRole for debugging
                Log.d("PostsAdapter", "Long-click detected. userRole: $userRole")

                if (userRole == "Admin") {
                    // Show confirmation dialog to delete the post
                    AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete") { _, _ ->
                            // Remove the post from the list
                            removePost(adapterPosition)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                true // Return true to indicate the long click was handled
            }
        }
    }

    // Method to remove a post
    private fun removePost(position: Int) {
        posts.removeAt(position)
        notifyItemRemoved(position)
        // Here you can also add additional logic to handle the post deletion in your database or backend
    }
}
