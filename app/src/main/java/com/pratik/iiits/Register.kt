package com.pratik.iiits

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.ktx.Firebase
import com.pratik.iiits.Models.UserModel

class Register : AppCompatActivity() {

    var postlist = arrayOf("Student")
    var defaultimageuri: String = "https://bit.ly/3T5Uk5W"
    var defaultstatus = "Hey There I'm Using this Application"
    private lateinit var arrayAdapter: ArrayAdapter<String>
    var post: String = "Student"
    var validmail = false
    var emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    lateinit var emailfield: EditText
    lateinit var passwordfield: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: FirebaseDatabase
    private lateinit var UserdatabaseRef: DatabaseReference
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        database = Firebase.database
        firestoreDb = FirebaseFirestore.getInstance() // Initialize Firestore
        emailfield = findViewById(R.id.emailbox_register)
        passwordfield = findViewById(R.id.passwordbox_register)
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.post_selector)
        autoCompleteTextView.requestFocus()
        arrayAdapter = ArrayAdapter<String>(this, R.layout.list_item, postlist)
        autoCompleteTextView.setAdapter<ArrayAdapter<String>>(arrayAdapter)
        autoCompleteTextView.onItemClickListener =
            OnItemClickListener { _, _, i, _ ->
                post = arrayAdapter.getItem(i).toString()
            }
        configureright()
        emailfield.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                validmail = emailPattern.matches(emailfield.text.toString().trim())
                Log.i("check", validmail.toString())
                checkvalidmail()
            }
        })
    }

    fun signup(view: View) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleresults(task)
        }
    }

    private fun handleresults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUi(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.fetchSignInMethodsForEmail(account.email.toString())
            .addOnCompleteListener(OnCompleteListener<SignInMethodQueryResult> { task ->
                val isNewUser = task.result.signInMethods!!.isEmpty()
                if (isNewUser) {
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            createnewAccount(account, null)
                        } else {
                            Toast.makeText(this, "" + it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Account Already Exists! Please Login.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun open_login(view: View?) {
        val intent = Intent(this, login_user::class.java)
        startActivity(intent)
        finish()
    }

    fun register_with_mail(view: View) {

        val mail = emailfield.text.toString()
        val pass = passwordfield.text.toString()
        if (mail.isNotEmpty() && pass.isNotEmpty() && validmail) {
            auth.fetchSignInMethodsForEmail(mail)
                .addOnCompleteListener(OnCompleteListener<SignInMethodQueryResult> { task ->
                    val isNewUser = task.result.signInMethods!!.isEmpty()
                    if (isNewUser) {
                        auth.createUserWithEmailAndPassword(mail, pass)
                            .addOnCompleteListener(this) { task ->
                                view.visibility=View.GONE
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success")
                                    var user = auth.currentUser

                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(user?.email?.substring(0, user.email!!.indexOf('@'))).build()

                                    user!!.updateProfile(profileUpdates)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d("profile update", "User profile updated.")
                                                user = auth.currentUser
                                                createnewAccount(null, user)
                                            } else {
                                                Log.d("profile update", "User profile not updated.")
                                            }
                                        }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                    view.visibility=View.VISIBLE
                                }
                            }
                    } else {
                        Toast.makeText(this, "Account Already Exists! Please Login.", Toast.LENGTH_SHORT).show()
                        view.visibility=View.VISIBLE
                    }
                })
        } else {
            Toast.makeText(this, "Please enter the appropriate credentials!", Toast.LENGTH_SHORT).show()
            view.visibility=View.VISIBLE
        }
    }

    fun configureright() {
        passwordfield.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        addRighDrawable(passwordfield, R.drawable.eye)
        addRighDrawable(emailfield, R.drawable.ic_baseline_assignment_late_24)
        passwordfield.onRightDrawableClicked {
            if (passwordfield.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                // Show password
                addRighDrawable(passwordfield, R.drawable.closed_eye)
                passwordfield.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Hide password
                addRighDrawable(passwordfield, R.drawable.eye)
                passwordfield.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    fun checkvalidmail() {
        if (validmail) {
            addRighDrawable(emailfield, R.drawable.ic_baseline_assignment_turned_in_24)
        } else addRighDrawable(emailfield, R.drawable.ic_baseline_assignment_late_24)
    }

    // Functions to add right drawable
    private fun addRighDrawable(editText: EditText, drawableid: Int) {
        val cancel = ContextCompat.getDrawable(this, drawableid)
        cancel?.setBounds(-10, 0, 80, 70)
        editText.setCompoundDrawables(null, null, cancel, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

    private fun createnewAccount(account: GoogleSignInAccount?, user: FirebaseUser?) {
        UserdatabaseRef = database.getReference("users").child(auth.uid.toString())
        val usermodel: UserModel
        if (user != null) {
            usermodel = UserModel(auth.uid, user.displayName, user.email, post, defaultimageuri, defaultstatus)
        } else {
            usermodel = UserModel(auth.uid, account?.displayName, account?.email, post, defaultimageuri, defaultstatus)
        }

        // Add user to Realtime Database
        UserdatabaseRef.setValue(usermodel).addOnCompleteListener {
            if (it.isSuccessful) {
                // Add user to Firestore
                firestoreDb.collection("users").document(auth.uid.toString()).set(usermodel)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registration Complete", Toast.LENGTH_SHORT).show()
                        open_login(null)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Firestore Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Realtime Database Registration Failed with uid ${auth.uid}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
