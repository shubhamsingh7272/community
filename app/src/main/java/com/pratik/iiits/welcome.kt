package com.pratik.iiits

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.ui.PlayerView
import com.pratik.iiits.databinding.ActivityWelcomeBinding

class welcome : AppCompatActivity() {


    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.getStartedButton.setOnClickListener {
            startActivity(Intent(this, login_user::class.java))
        }


    }

    override fun onDestroy() {
        super.onDestroy()
    }
    fun start(view:View) {
        startActivity(Intent(this, login_user::class.java))
    }
}
