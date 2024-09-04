package com.pratik.iiits.Models

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import com.google.j2objc.annotations.Property

data class Post(var description: String="",
                         var image_url:String="",
                         var creation_time_ms:Long=0,
                         var user:UserModel?=null)