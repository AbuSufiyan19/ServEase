package com.project.mad

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var spinnerUserType: Spinner
    private lateinit var editTextPassword: EditText
    private lateinit var rePassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var progressDialog: AlertDialog? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        var login = findViewById<TextView>(R.id.gotosignup)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        spinnerUserType = findViewById(R.id.spinnerUserType)
        editTextPassword = findViewById(R.id.editTextPassword)
        rePassword = findViewById(R.id.repassword)
        buttonSignUp = findViewById(R.id.Signup)
        login.setOnClickListener(View.OnClickListener {
            var i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        })


        // Populate the Spinner with options
        val userTypeOptions = arrayOf("Select","Customer", "Service Man")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserType.adapter = adapter



        // Set onFocusChangeListener for editTextUsername
        editTextUsername.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val username = editTextUsername.text.toString().trim()
                if (!validateUserName(username)) {
                    editTextUsername.error = "Enter a valid username" // Adjust the error message as needed
                } else {
                    editTextUsername.error = null
                }
            }
        }

// Set onFocusChangeListener for editTextEmail
        editTextEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = editTextEmail.text.toString().trim()
                if (!validateEmail(email)) {
                    editTextEmail.error = "Enter a valid email address" // Adjust the error message as needed
                } else {
                    editTextEmail.error = null
                }
            }
        }

// Set onFocusChangeListener for editTextPhoneNumber
        editTextPhoneNumber.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phoneNumber = editTextPhoneNumber.text.toString().trim()
                if (!validatePhoneNumber(phoneNumber)) {
                    editTextPhoneNumber.error = "Enter a valid phone number" // Adjust the error message as needed
                } else {
                    editTextPhoneNumber.error = null
                }
            }
        }

// Set onFocusChangeListener for editTextPassword
        editTextPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = editTextPassword.text.toString().trim()
                if (!validatePassword(password)) {
                    editTextPassword.error = "Password must contains 1 uppercase, 1 lowercase, 1 special character, 1 number and with a minimum of 8 characters." // Adjust the error message as needed
                } else {
                    editTextPassword.error = null
                }
            }
        }

// Set onFocusChangeListener for rePassword
        rePassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = editTextPassword.text.toString().trim()
                val confirmPassword = rePassword.text.toString().trim()
                if (confirmPassword != password) {
                    rePassword.error = "Passwords do not match" // Adjust the error message as needed
                } else {
                    rePassword.error = null
                }
            }
        }


        buttonSignUp.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val phoneNumber = editTextPhoneNumber.text.toString().trim()
            val userType = spinnerUserType.selectedItem.toString()
            val password = editTextPassword.text.toString().trim()
            val rePassword = rePassword.text.toString().trim()


            if(username.isNotEmpty() && validateUserName(username)) {
                if (email.isNotEmpty() && validateEmail(email)) {
                    if(!userType.equals("Select")) {
                        if (phoneNumber.isNotEmpty() && validatePhoneNumber(phoneNumber)) {
                            if (password.isNotEmpty() && validatePassword(password)) {
                                if (rePassword.isNotEmpty() && password == rePassword) {
                                    showProgressDialog()

//                                    val dialogView = LayoutInflater.from(this)
//                                        .inflate(R.layout.dialog_signupwait, null)
//                                    val dialogBuilder = AlertDialog.Builder(this)
//                                        .setView(dialogView)
//                                    val dialog = dialogBuilder.create()
//                                    dialog.show()
                                    signUp(username, email, phoneNumber, userType, password)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Passwords do not match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(this, "Enter a valid password", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }else{
                        Toast.makeText(this, "Select User Type", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                else {
                    Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "Enter a valid username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signupwait, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        progressDialog = dialogBuilder.create()
        progressDialog?.show()
    }
    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }


    private fun validateUserName(username: String): Boolean {
        val usernamePattern: Pattern =
            Pattern.compile("^[a-zA-Z0-9_ ]{5,16}$")
        return usernamePattern.matcher(username).matches()
    }


    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        val phonePattern: Pattern = Pattern.compile("\\d{10}") // Matches exactly 10 digits
        return phonePattern.matcher(phoneNumber).matches()
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }

    private fun validatePassword(password: String): Boolean {
        val pattern: Pattern =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return pattern.matcher(password).matches()
    }

    private fun signUp(
        username: String,
        email: String,
        phoneNumber: String,
        userType: String,
        password: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Email verification link sent successfully
                                // You can display a message to the user to check their email inbox
                                if (userId != null) {
                                    val userDetails = mapOf(
                                        "userId" to userId,
                                        "username" to username,
                                        "email" to email,
                                        "phoneNumber" to phoneNumber
                                    )

                                    val databaseReference =
                                        firebaseDatabase.reference.child(if (userType == "Customer") "users" else "serviceMan")
                                    databaseReference.child(userId)
                                        .setValue(userDetails)
                                        .addOnSuccessListener {
                                            val rat = "0.0"
                                            val ratingUpdate = mapOf("rating" to rat)
                                            databaseReference.child(userId).updateChildren(ratingUpdate)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        this,
                                                        "Registered successful",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    val intent = Intent(this, LoginActivity::class.java).apply {
                                                        putExtra("emailverify","true")
                                                    }
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        this,
                                                        "Error storing rating: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                this,
                                                "Error saving user details: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                // Failed to send verification email
                                Toast.makeText(
                                    this,
                                    "Failed to send verification email: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Check if the failure is due to email already in use
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        dismissProgressDialog()
                        Toast.makeText(
                            this,
                            "The email address is already in use by another account.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Sign up failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}