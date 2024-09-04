package com.pratik.iiits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class EditProfile : AppCompatActivity() {
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
    }

    fun savedata(view: View) {}
    fun avatarchange(view: View) {}
    fun close(view: View) {}
}