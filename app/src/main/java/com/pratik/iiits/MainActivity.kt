package com.pratik.iiits


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pratik.iiits.Marketplace.BuyActivity
import com.pratik.iiits.Timetable.ScheduleActivity
import com.pratik.iiits.chatapp.ChatAppHome
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var daytime: TextView? = null
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var userimage: CircleImageView
    lateinit var hiusername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hook()
        mAuth = FirebaseAuth.getInstance()
        daytime = findViewById(R.id.daytime)



        setGreeting()




        val ref: DatabaseReference = database.getReference().child("users").child(auth.uid.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Picasso.get().load(snapshot.child("imageUri").value.toString()).into(userimage)
                hiusername.text = "Hi " + snapshot.child("name").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStart() {
        super.onStart()
        val rootLayout: RelativeLayout = findViewById(R.id.root_layout)
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        rootLayout.startAnimation(animation)

        val rootLayout1: NestedScrollView = findViewById(R.id.root_layout1)

        // Array of drawable resources
        val images = intArrayOf(
            R.drawable.back1,
            R.drawable.back2,
            R.drawable.back3,
            R.drawable.back4,
            R.drawable.back5,
            R.drawable.back6,
            R.drawable.back7,
            R.drawable.back8,


            )


        // Get a random image
        val randomImage = images[Random.nextInt(images.size)]


        // Set the random image as background
        rootLayout1.setBackgroundResource(randomImage)
    }

    private fun setGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val greeting = when {
            hour in 6..11 -> "Good morning!"
            hour in 12..17 -> "Good afternoon!"
            hour in 18..21 -> "Good evening!"
            else -> "Good night!"
        }
        daytime!!.text = greeting
    }

    fun openEvents(view: View?) {
        startActivity(Intent(this@MainActivity, EventsActivity::class.java))
    }

    fun openCalender(view: View) {
        startActivity(Intent(this@MainActivity, ScheduleActivity::class.java))
    }

    fun opennotesactivity(view: View?) {
        val intent = Intent(this@MainActivity, NotesActivity::class.java)
        startActivity(intent)
    }

    fun openprofile(view: View?) {
        startActivity(Intent(this@MainActivity, ProfilePage::class.java).putExtra("authuid", auth.uid.toString()).putExtra("self", true))
    }

    fun openchats(view: View?) {
        val intent = Intent(this@MainActivity, ChatAppHome::class.java)
        startActivity(intent)
    }

    fun openGroups(view: View?) {
        val intent = Intent(this@MainActivity, CategoriesActivity::class.java)
        startActivity(intent)
    }

    fun openMarket(view: View?) {
        val intent = Intent(this@MainActivity, BuyActivity::class.java)
        startActivity(intent)
    }

    fun openMaps(view: View?) {
        val intent = Intent(this@MainActivity, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun hook() {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        userimage = findViewById(R.id.userimage)
        hiusername = findViewById(R.id.username)
    }
}
