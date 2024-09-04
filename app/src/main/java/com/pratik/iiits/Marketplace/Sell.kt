package com.pratik.iiits.Marketplace

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pratik.iiits.R
import java.util.UUID

class Sell : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val imageUris: ArrayList<Uri> = ArrayList()
    private lateinit var imageAdapter: ImageAdapter

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etEmail: EditText // Add this line
    private lateinit var btnUploadImages: Button
    private lateinit var btnSubmit: Button
    private lateinit var imagesPreviewContainer: LinearLayout

    private var username: String? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        etTitle = findViewById(R.id.item_title)
        etDescription = findViewById(R.id.item_description)
        etPrice = findViewById(R.id.item_price)
        etEmail = findViewById(R.id.etEmail) // Add this line
        btnUploadImages = findViewById(R.id.upload_images_button)
        btnSubmit = findViewById(R.id.submit_button)
        imagesPreviewContainer = findViewById(R.id.images_preview_container)

        btnUploadImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        btnSubmit.setOnClickListener {
            btnSubmit.isEnabled = false // Disable the button on click
            uploadItem()
        }
        fetchUsername { fetchedUsername ->
            // Handle the fetched username here if needed
            // For example, you can log it to verify it's fetched successfully
            Log.d("SellActivity", "Fetched username: $fetchedUsername")
        }
    }
    private fun fetchUsername(callback: (String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("name")
                callback(username)
            }
            .addOnFailureListener {
                Log.e("SellActivity", "Error fetching username: ", it)
                callback(null)
            }
    }

    private fun uploadItem() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val price = etPrice.text.toString().trim()
        val email = etEmail.text.toString().trim() // Add this line

        if (title.isEmpty() || description.isEmpty() || price.isEmpty() || email.isEmpty() || imageUris.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            btnSubmit.isEnabled = true // Enable the button if input is invalid
            return
        }

        // Call fetchUsername with a callback
        fetchUsername { fetchedUsername ->
            // Check if the username is fetched successfully
            if (fetchedUsername != null) {
                val userId = auth.currentUser?.uid ?: return@fetchUsername
                val itemId = UUID.randomUUID().toString()
                val imageUrls = mutableListOf<String>()

                imageUris.forEach { uri ->
                    val imageRef = storage.reference.child("images/$itemId/${UUID.randomUUID()}.jpg")
                    imageRef.putFile(uri)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                imageUrls.add(downloadUri.toString())
                                if (imageUrls.size == imageUris.size) {
                                    // Once all images are uploaded, save the item to Firestore
                                    saveItemToFirestore(title, description, price, email, imageUrls, userId, fetchedUsername, itemId)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            btnSubmit.isEnabled = true // Enable the button if image upload fails
                        }
                }
            } else {
                // Inform the user if the username is not fetched
                Toast.makeText(this, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true // Enable the button if username fetch fails
            }
        }
    }

    private fun saveItemToFirestore(
        title: String,
        description: String,
        price: String,
        email: String, // Add this line
        imageUrls: List<String>,
        userId: String,
        username: String,
        itemId: String
    ) {
        val item = hashMapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "email" to email, // Add this line
            "imageUrls" to imageUrls,
            "userId" to userId,
            "username" to username,
            "itemId" to itemId
        )

        db.collection("items").document(itemId).set(item)
            .addOnSuccessListener {
                Toast.makeText(this, "Item uploaded successfully", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true // Enable the button after successful upload
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload item", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true // Enable the button if item upload fails
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            data?.let {
                if (it.clipData != null) {
                    val count = it.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = it.clipData!!.getItemAt(i).uri
                        imageUris.add(imageUri)
                    }
                } else if (it.data != null) {
                    val imageUri = it.data!!
                    imageUris.add(imageUri)
                }
                updateImagePreview()
            }
        }
    }

    private fun updateImagePreview() {
        imagesPreviewContainer.removeAllViews()
        imageUris.forEach { uri ->
            val imageView = ImageView(this)
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
            imageView.setImageBitmap(scaledBitmap)
            val layoutParams = LinearLayout.LayoutParams(200, 200) // Set appropriate size
            layoutParams.setMargins(8, 8, 8, 8)
            imageView.layoutParams = layoutParams
            imagesPreviewContainer.addView(imageView)
        }
    }

}