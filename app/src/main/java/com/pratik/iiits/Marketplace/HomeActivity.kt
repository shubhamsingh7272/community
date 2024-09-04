package com.pratik.iiits.Marketplace

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pratik.iiits.R

class HomeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)






    }
    fun buy(view: View) {
        startActivity(Intent(this@HomeActivity, BuyActivity::class.java))
    }
    fun sell(view: View) {
        startActivity(Intent(this@HomeActivity,Sell::class.java))
    }
}