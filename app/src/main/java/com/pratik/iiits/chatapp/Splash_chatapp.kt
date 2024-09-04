package com.pratik.iiits.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.pratik.iiits.R


class Splash_chatapp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_chatapp)

        val handler = Handler()
        handler.postDelayed(Runnable {

            startActivity(Intent(this@Splash_chatapp,ChatAppHome::class.java))
            finish()
        }, 1500)
    }
}