package com.pratik.iiits

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pratik.iiits.Models.Post
import com.pratik.iiits.Models.UserModel

class createPost : AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var image: ImageView
    private lateinit var description: EditText
    private var user1: UserModel? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var submitButton: Button
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        submitButton = findViewById(R.id.createBtn)
        image = findViewById(R.id.postImage)
        image.setImageResource(R.drawable.placeholder_image)
        storageRef = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                user1 = userSnapshot.toObject(UserModel::class.java)
                Log.d(ContentValues.TAG, "Username: $user1")
            }
    }

    fun pickImage(view: View) {
        val imagePicker = Intent(Intent.ACTION_GET_CONTENT)
        imagePicker.type = "image/*"
        if (imagePicker.resolveActivity(packageManager) != null) {
            startActivityForResult(imagePicker, 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val image = findViewById<ImageView>(R.id.postImage)
        if (requestCode == 1234) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri = data?.data
                image.setImageURI(imageUri)
                Log.d("imageUri", imageUri.toString())
            }
        }
    }

    fun submit(view: View) {
        submitButton.isEnabled = false
        loadingProgressBar.visibility = View.VISIBLE // Show the progress bar
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            loadingProgressBar.visibility = View.INVISIBLE // Hide the progress bar
            return
        }
        description = findViewById(R.id.description)
        if (description.text.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            loadingProgressBar.visibility = View.INVISIBLE // Hide the progress bar
            return
        }
        if (user1 == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            loadingProgressBar.visibility = View.INVISIBLE // Hide the progress bar
            return
        }

        val photoRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        photoRef.putFile(imageUri!!)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                photoRef.downloadUrl
            }.continueWithTask { downloadUrlTask ->

                val post = Post(
                    description.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    user1
                )
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                submitButton.isEnabled = true
                loadingProgressBar.visibility = View.INVISIBLE // Hide the progress bar
                if (!postCreationTask.isSuccessful) {
                    Log.e("Exception", "Failed to save post", postCreationTask.exception)
                }
                val eventsIntent = Intent(this, EventsActivity::class.java)
                startActivity(eventsIntent)
                finish()
            }
    }
}
