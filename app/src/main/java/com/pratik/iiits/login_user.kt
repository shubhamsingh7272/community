package com.pratik.iiits

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class login_user : AppCompatActivity() {

    private var validmail = false
    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    private lateinit var passwordbox: EditText
    private lateinit var emailbox: EditText
    private lateinit var phonebox: EditText
    private lateinit var otpbox: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loadingProgressBar: ProgressBar
    private var verificationId: String? = null
    private lateinit var ccp: CountryCodePicker
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var flag: Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        emailbox = findViewById(R.id.emailbox)
        passwordbox = findViewById(R.id.passwordbox)
        phonebox = findViewById(R.id.phonebox)
        otpbox = findViewById(R.id.otpbox)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        ccp=findViewById(R.id.ccp)
        flag=0

        configureright()
        emailbox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                validmail = emailPattern.matches(emailbox.text.toString().trim())
                Log.i("check", validmail.toString())
                checkvalidmail()
            }
        })

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        sendOtpButton.setOnClickListener { sendVerificationCode() }
        verifyOtpButton.setOnClickListener { verifyCode() }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification succeeded
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Verification failed
                Toast.makeText(this@login_user, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Code has been sent
                this@login_user.verificationId = verificationId
                Toast.makeText(this@login_user, "OTP Sent", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun verifyCode() {
        val code = otpbox.text.toString().trim()
        if (code.isNotEmpty() && verificationId != null) {
            loadingProgressBar.visibility = View.VISIBLE
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loadingProgressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success
                    val user = task.result?.user
                    flag=1
                } else {
                    // Sign in failed
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun signin(view: View) {
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
            } else {
                Toast.makeText(this, "Google Sign-In account is null", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Google Sign-In failed: ${task.exception}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.fetchSignInMethodsForEmail(account.email.toString())
            .addOnCompleteListener { task ->
                Log.i("user2", FirebaseAuth.getInstance().uid.toString())
                Log.i("user", task.result.signInMethods.toString())
                val isNewUser = FirebaseAuth.getInstance().uid.isNullOrBlank()
                if (isNewUser) {
                    Toast.makeText(this, "Account Doesn't Exist! Please Register.", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            login(account, null)
                        } else {
                            Toast.makeText(this, "${it.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    fun login_with_email(view: View) {

        val mail = emailbox.text.toString()
        val pass = passwordbox.text.toString()
        if(true){
            view.visibility = View.GONE
            if (mail.isNotEmpty() && pass.isNotEmpty() && validmail) {
                auth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this) { task ->
                        loadingProgressBar.visibility = View.INVISIBLE
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            login(null, user)
                        } else {
                            when (task.exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    Toast.makeText(baseContext, "User does not exist.", Toast.LENGTH_SHORT).show()
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(baseContext, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                                }
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(baseContext, "Email is already in use.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            view.visibility = View.VISIBLE
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter all credentials properly!", Toast.LENGTH_SHORT).show()
                loadingProgressBar.visibility = View.INVISIBLE
                view.visibility = View.VISIBLE
            }
        }else{
            Toast.makeText(baseContext,"Please verify your mobile number",Toast.LENGTH_SHORT).show()
        }

    }

    private fun login(account: GoogleSignInAccount?, user: FirebaseUser?) {
        startActivity(Intent(this@login_user, MainActivity::class.java))
        finishAffinity()
    }

    fun configureright(){
        passwordbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        addRighDrawable(passwordbox, R.drawable.eye)
        addRighDrawable(emailbox, R.drawable.ic_baseline_assignment_late_24)
        passwordbox.onRightDrawableClicked {
            if (passwordbox.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD){
                addRighDrawable(passwordbox, R.drawable.closed_eye)
                passwordbox.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                addRighDrawable(passwordbox, R.drawable.eye)
                passwordbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    fun open_register(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun checkvalidmail() {
        if (validmail){
            addRighDrawable(emailbox, R.drawable.ic_baseline_assignment_turned_in_24)
        } else {
            addRighDrawable(emailbox, R.drawable.ic_baseline_assignment_late_24)
        }
    }

    private fun addRighDrawable(editText: EditText, drawableid : Int) {
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

    fun sendVerificationCode() {

        val phoneNumber = phonebox.text.toString().trim()
        if (phoneNumber.isNotEmpty()) {
            otpbox.visibility = View.VISIBLE
            sendOtpButton.visibility = View.GONE
            verifyOtpButton.visibility = View.VISIBLE
//            loadingProgressBar.visibility = View.VISIBLE
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            otpbox.visibility = View.GONE
            sendOtpButton.visibility = View.VISIBLE
            verifyOtpButton.visibility = View.GONE
        }
    }

}
