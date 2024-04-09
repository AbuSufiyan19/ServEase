package com.project.mad

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var spinnerUserType: Spinner
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        var login=findViewById<TextView>(R.id.gotosignup)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        spinnerUserType = findViewById(R.id.spinnerUserType)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignUp = findViewById(R.id.Signup)
        login.setOnClickListener(View.OnClickListener {
            var i=Intent(this,LoginActivity::class.java)
            startActivity(i)
        })


        // Populate the Spinner with options
        val userTypeOptions = arrayOf("Customer", "Service Man")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserType.adapter = adapter


        buttonSignUp.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val phoneNumber = editTextPhoneNumber.text.toString().trim()
            val userType = spinnerUserType.selectedItem.toString()
            val password = editTextPassword.text.toString().trim()

            if (validateEmail(email) && validatePhoneNumber(phoneNumber) && validatePassword(password) && username.isNotEmpty() && email.isNotEmpty() && phoneNumber.isNotEmpty()
                && password.isNotEmpty()
            ) {
                signUp(username, email, phoneNumber, userType, password)
            } else {
                Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        val phonePattern: Pattern = Pattern.compile("\\d{10}") // Matches exactly 10 digits
        return phonePattern.matcher(phoneNumber).matches()
    }
    
    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }
    private fun validatePassword(password: String): Boolean {
        val pattern: Pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return pattern.matcher(password).matches()
    }

    private fun signUp(username: String, email: String, phoneNumber: String, userType: String, password: String) {
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

                                    val databaseReference = firebaseDatabase.reference.child(if (userType == "Customer") "users" else "serviceMan")
                                    databaseReference.child(userId)
                                        .setValue(userDetails)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Error saving user details: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                // Failed to send verification email
                                Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                            }
                        }


                } else {
                    // Check if the failure is due to email already in use
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "The email address is already in use by another account.",
                            Toast.LENGTH_SHORT
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