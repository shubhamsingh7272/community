package com.pratik.iiits.notes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.pratik.iiits.R
import com.pratik.iiits.Models.Post

class PostDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_POST_JSON = "post_json"

        fun newInstance(post: Post): PostDetailDialogFragment {
            val args = Bundle()
            val postJson = Gson().toJson(post)
            args.putString(ARG_POST_JSON, postJson)
            val fragment = PostDetailDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)
        val postJson = arguments?.getString(ARG_POST_JSON)
        val post = Gson().fromJson(postJson, Post::class.java)

        val tvPostTitle = view.findViewById<TextView>(R.id.tvPostTitle)
        val ivPostImage = view.findViewById<ImageView>(R.id.ivPostImage)
        val tvPostDescription = view.findViewById<TextView>(R.id.tvPostDescription)

        post?.let {
            tvPostTitle.text = it.user?.name ?: "No Name"
            tvPostDescription.text = it.description ?: "No Description"
            Glide.with(this).load(it.image_url).into(ivPostImage)
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.CustomDialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
        }
    }
}
