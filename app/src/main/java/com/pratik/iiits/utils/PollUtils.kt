package com.pratik.iiits.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pratik.iiits.Models.Poll

object PollUtils {
    private const val PREFS_NAME = "poll_prefs"
    private const val POLLS_KEY = "previous_polls"

    private val gson: Gson = GsonBuilder().create()

    fun savePolls(context: Context, polls: List<Poll>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = gson.toJson(polls)
        editor.putString(POLLS_KEY, json)
        editor.apply()
    }

    fun loadPolls(context: Context): List<Poll> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(POLLS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Poll>>() {}.type
        return gson.fromJson(json, type)
    }
}
