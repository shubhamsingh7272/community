package com.pratik.iiits.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pratik.iiits.Models.Post

object PostUtils {
    private const val PREFS_NAME = "post_prefs"
    private const val POSTS_KEY = "previous_posts"

    private val gson = Gson()

    fun savePosts(context: Context, posts: List<Post>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = gson.toJson(posts)
        editor.putString(POSTS_KEY, json)
        editor.apply()
    }

    fun loadPosts(context: Context): List<Post> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(POSTS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Post>>() {}.type
        return gson.fromJson(json, type)
    }
}
