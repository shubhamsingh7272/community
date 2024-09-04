package com.pratik.iiits

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Models.Poll
import com.pratik.iiits.Models.UserModel

class CreatePoll : AppCompatActivity() {
    private lateinit var pollQuestion: TextInputEditText
    private lateinit var optionsContainer: LinearLayout
    private var currentUser: UserModel? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_poll)

        // Initialize views
        submitButton = findViewById(R.id.createPollBtn)
        pollQuestion = findViewById(R.id.pollQuestion)
        optionsContainer = findViewById(R.id.optionsContainer)
        firestoreDb = FirebaseFirestore.getInstance()

        // Fetch current user information from Firestore
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                currentUser = userSnapshot.toObject(UserModel::class.java)
                Log.d(ContentValues.TAG, "Username: $currentUser")
            }

        // Add initial option fields
        addOptionField()
        addOptionField()
    }

    fun addOption(view: View) {
        addOptionField()
    }

    private fun addOptionField() {
        val radioButton = RadioButton(this)
        radioButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }
        radioButton.text = "Option ${optionsContainer.childCount / 2 + 1}"
        radioButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        val textInputLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this@CreatePoll, android.R.color.black))
            hint = "Option ${optionsContainer.childCount / 2 + 1}"
            setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
        }

        val textInputEditText = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(ContextCompat.getColor(this@CreatePoll, android.R.color.black))
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@CreatePoll, android.R.color.black))
        }

        textInputEditText.addTextChangedListener { text ->
            radioButton.text = text
        }

        textInputLayout.addView(textInputEditText)
        optionsContainer.addView(radioButton)
        optionsContainer.addView(textInputLayout)
    }

    fun submitPoll(view: View) {
        submitButton.isEnabled = false
        val question = pollQuestion.text.toString()

        if (question.isEmpty()) {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            return
        }

        val options = mutableListOf<String>()
        val voteCounts = mutableListOf<Int>()
        val voters = mutableMapOf<String, Int>()

        for (i in 0 until optionsContainer.childCount step 2) {
            val textInputLayout = optionsContainer.getChildAt(i + 1) as TextInputLayout
            val optionText = textInputLayout.editText?.text.toString()
            if (optionText.isNotEmpty()) {
                options.add(optionText)
                voteCounts.add(0) // Initialize vote count to 0 for each option
            }
        }

        if (options.size < 2) {
            Toast.makeText(this, "Please enter at least two options", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            return
        }

        val poll = Poll(
            question,
            options,
            voteCounts,
            voters,
            System.currentTimeMillis(),
            currentUser
        )

        firestoreDb.collection("polls").add(poll)
            .addOnCompleteListener { pollCreationTask ->
                submitButton.isEnabled = true
                if (!pollCreationTask.isSuccessful) {
                    Log.e("Exception", "Failed to save poll", pollCreationTask.exception)
                    Toast.makeText(this, "Failed to save poll", Toast.LENGTH_SHORT).show()
                } else {
                    val eventsIntent = Intent(this, EventsActivity::class.java)
                    startActivity(eventsIntent)
                    finishAffinity()
                }
            }
    }
}
