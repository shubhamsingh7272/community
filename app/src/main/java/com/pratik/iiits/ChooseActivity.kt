package com.pratik.iiits

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pratik.iiits.databinding.ActivityChooseBinding

class ChooseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listeners for the buttons
        binding.createPostButton.setOnClickListener {
            createPost()
        }

        binding.createPollButton.setOnClickListener {
            createPoll()
        }
    }

    private fun createPost() {
        val intent = Intent(this, createPost::class.java)
        startActivity(intent)
    }

    private fun createPoll() {
        val intent = Intent(this, CreatePoll::class.java)
        startActivity(intent)
    }
}
