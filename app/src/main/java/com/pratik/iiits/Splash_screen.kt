package com.pratik.iiits

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Splash_screen : AppCompatActivity() {
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Find views
        val logo1: ImageView = findViewById(R.id.logo1)
        val logo2: ImageView = findViewById(R.id.logo2)
        val textView: TextView = findViewById(R.id.textView)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        // Apply fade-in animation
        logo1.startAnimation(fadeIn)
        textView.startAnimation(fadeIn)

        handler = Handler()
        handler!!.postDelayed({
            // Apply fade-out animation
            logo1.startAnimation(fadeOut)
            logo2.startAnimation(fadeOut)
            textView.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    // Navigate to appropriate screen based on authentication status
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        startActivity(Intent(this@Splash_screen, welcome::class.java))
                    } else {
                        startActivity(Intent(this@Splash_screen, MainActivity::class.java))
                    }
                    finish()
                }
            })
        }, 1500)
    }
}
