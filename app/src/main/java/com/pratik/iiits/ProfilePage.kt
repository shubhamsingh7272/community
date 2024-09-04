package com.pratik.iiits


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.InvalidationTracker
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.pratik.iiits.Models.Post
import com.pratik.iiits.Role.AdminDashboardActivity
import com.pratik.iiits.Role.RoleAdapter
import com.pratik.iiits.Role.RoleRequest
import com.pratik.iiits.Role.RoleViewModel

import com.pratik.iiits.Role.UserRoleManagementActivity
import com.pratik.iiits.chatapp.ChatScreen
import com.pratik.iiits.notes.Adapters.PostsAdapter
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfilePage : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var authid: String
    lateinit var userimage: CircleImageView
    lateinit var username: TextView
    lateinit var useremail: TextView
    lateinit var userpost: TextView
    lateinit var useremail2: TextView
    lateinit var bio: TextView
    lateinit var rolesTextView: TextView
    lateinit var btn1: ImageButton
    lateinit var uri: String
    private lateinit var role:ImageButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private lateinit var rolesRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private val roleList = mutableListOf<RoleRequest>()

    private val roleViewModel: RoleViewModel by viewModels()
    private lateinit var roleAdapter: RoleAdapter
    private lateinit var flexboxLayout: FlexboxLayout



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        // Initialize views
        hook()
        checkIfAdmin()

        // Fetch user ID passed from previous activity or deep link
        authid = intent.getStringExtra("authuid").toString()

        val self = intent.getBooleanExtra("self", false)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if the activity was launched from a deep link
        val uri1 = intent.data
        if (uri1 != null) {
            val parameters: List<String> = uri1.pathSegments
            authid = parameters[parameters.size - 1]
        }

        // Reference to Firebase Database
        val ref = database.getReference("users").child(authid)

        // Set up listener to fetch user data
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Populate UI elements with user data
                uri = snapshot.child("imageUri").value.toString()
                Picasso.get().load(uri).into(userimage)
                username.text = snapshot.child("name").value.toString()
                userpost.text = snapshot.child("postinIIIT").value.toString()
                useremail.text = snapshot.child("email").value.toString()
                useremail2.text = snapshot.child("email").value.toString()
                bio.text = snapshot.child("status").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfilePage, "Failed to load user data", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        // Set up RecyclerView for posts
        posts = mutableListOf()
        adapter = PostsAdapter(this, posts,"Admin")
        val rvPosts = findViewById<RecyclerView>(R.id.rvPosts)
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

        // Initialize Firestore instance
        firestoreDb = FirebaseFirestore.getInstance()

        flexboxLayout = findViewById(R.id.flexbox_layout)

        roleViewModel.roles.observe(this, Observer { roles ->
            roleAdapter = RoleAdapter(this, roles)
            roleAdapter.addViewsToFlexboxLayout(flexboxLayout)
        })

//        fetchAssignedRoles()


        // Fetch user's posts from Firestore
        val postsReference = firestoreDb.collection("posts")
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)
            .whereEqualTo("user.username", username.text.toString())

        postsReference.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w("ProfilePage", "Listen failed.", e)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.d("ProfilePage", "Post: $post")
            }
        }

        // Set up button to open image picker
        findViewById<ImageButton>(R.id.uploadProfilePicButton).setOnClickListener {
            openImagePicker()
        }
    }

    private fun hook() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userimage = findViewById(R.id.profileimage)
        username = findViewById(R.id.profilename)
        useremail = findViewById(R.id.profileemail)
        useremail2 = findViewById(R.id.profileemail2)
        userpost = findViewById(R.id.profilepost)
        bio = findViewById(R.id.statusbio)
        role=findViewById(R.id.role)
        firestore = FirebaseFirestore.getInstance()
        btn1 = findViewById(R.id.meassgeoredit)
    }

    private fun checkIfAdmin() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val postInIIIT = document.getString("postinIIIT")
                        Log.e(ContentValues.TAG, postInIIIT.toString())
                        if (postInIIIT == "Admin") {
                            role.visibility = Button.VISIBLE
                            findViewById<Space>(R.id.roleSpace).visibility=Button.VISIBLE
                        } else {
                            role.visibility = Button.GONE
                            findViewById<Space>(R.id.roleSpace).visibility=Button.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error fetching user details: $e")
                    role.visibility = Button.GONE
                    findViewById<Space>(R.id.roleSpace).visibility=Button.GONE
                }
        }
    }

    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            uploadImageToFirebase(filePath)
        }
    }
//    private fun fetchAssignedRoles() {
//        val userId = auth.currentUser?.uid ?: return
//        firestoreDb.collection("roleRequests")
//            .whereEqualTo("userId", userId)
//            .whereEqualTo("status", "approved")
//            .get()
//            .addOnSuccessListener { result ->
//                roleList.clear()
//                for (document in result) {
//                    val role = document.toObject(RoleRequest::class.java)
//                    role.id = document.id
//                    roleList.add(role)
//                }
//                roleAdapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to fetch assigned roles", Toast.LENGTH_SHORT).show()
//            }
//    }




    private fun uploadImageToFirebase(filePath: Uri?) {
        if (filePath != null) {
            val ref =
                FirebaseStorage.getInstance().reference.child("profileImages/${auth.currentUser?.uid}")
            ref.putFile(filePath)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        database.getReference("users").child(auth.currentUser?.uid.toString())
                            .child("imageUri").setValue(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Log.e("ProfilePage", "Failed to upload image", it)
                }
        }
    }

    fun logout(view: View) {
        val dialog: BottomSheetDialog =
            BottomSheetDialog(this@ProfilePage, R.style.BottomSheetStyle)
        dialog.setContentView(R.layout.logout_dailog)
        dialog.show()
        val yesBtn = dialog.findViewById<TextView>(R.id.yesbtn)
        val noBtn = dialog.findViewById<TextView>(R.id.nobtn)

        yesBtn?.setOnClickListener {
            // Sign out from Firebase Authentication
            auth.signOut()

            // Sign out from Google Sign-In (if applicable)
            googleSignInClient.signOut().addOnCompleteListener(this) {
                // Redirect the user to the login screen or perform any other necessary actions
                startActivity(Intent(this@ProfilePage, welcome::class.java))
                finishAffinity() // Close the current activity to prevent the user from returning to it using the back button
            }
            dialog.dismiss()
        }
        noBtn?.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun userrole(view: View?) {
        val intent = Intent(this@ProfilePage, UserRoleManagementActivity::class.java)
        startActivity(intent)
    }

    fun admin(view: View?) {
        val intent = Intent(this@ProfilePage, AdminDashboardActivity::class.java)
        startActivity(intent)
    }

    fun mailme(view: View?) {}
    fun closeprofile(view: View?) {
        finish()
    }

    fun shareprofile(view: View?) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "" + intent.getStringExtra("name").toString())
        val ss =
            Html.fromHtml("Find my User Details through this link, https://www.iiits.in/$authid")
        emailIntent.putExtra(Intent.EXTRA_TEXT, ss.toString())
        emailIntent.type = "text/plain"
        startActivity(Intent.createChooser(emailIntent, "Send to friend"))
    }

    fun messageme(view: View?) {
        val intent = Intent(this@ProfilePage, ChatScreen::class.java)
        intent.putExtra("name", username.text.toString())
        intent.putExtra("ReciverImage", uri)

    }

    fun openROLE(view: View) {
        startActivity(Intent(this@ProfilePage, SearchUsersActivity::class.java))
    }
}