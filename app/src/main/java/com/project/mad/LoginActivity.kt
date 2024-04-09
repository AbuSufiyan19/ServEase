package com.project.mad

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextLoginEmail: EditText
    private lateinit var editTextLoginPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewSignUp: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("users") // "users" is the node in the Realtime Database

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail)
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignUp = findViewById(R.id.textViewSignUp)

        buttonLogin.setOnClickListener {
            val email = editTextLoginEmail.text.toString().trim()
            val password = editTextLoginPassword.text.toString().trim()

            if (validateEmail(email) && password.isNotEmpty()) {
                loginAuthentication(email, password)
            } else {
                Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }


        textViewSignUp.setOnClickListener {
            // Navigate to SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


//    private fun login(email: String, password: String) {
//        // Check if the email is an admin
//        if (email == "admin123@gmail.com" && password == "admin@123") {
//            navigateToAdminHomePage()
//        } else {
//            // Check if the email is in the service man collection
//            loginAuthentication(email, password)
//        }
//    }

    private fun loginAuthentication(email: String, password: String) {
        // User is not logged in, proceed with login
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Update UI based on user's authentication status
                        establishSession(user)
                        updateUI(user)
                    } else {
                        // If login fails, display a message to the user.
                        Toast.makeText(
                            this,
                            "Authentication failed: Email not verified, check Inbox and verify ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // If login fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun establishSession(user: FirebaseUser) {
        val userToken = user.uid // Retrieve user's unique ID token
        val userEmail = user.email // Retrieve user's email

        // Store the token and email securely on the client-side (e.g., in shared preferences)
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userToken", userToken)
        userEmail?.let { editor.putString("userEmail", it) }

        // Retrieve username from Firebase Realtime Database and store it into SharedPreferences
        val userId = user.uid
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // If user exists, retrieve the username
                        val username = dataSnapshot.child("username").getValue(String::class.java)
                        username?.let { editor.putString("username", it) }
                        editor.apply()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled
                }
            })

        editor.apply()
    }


    private fun clearSession() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
        }
    }



    private fun checkServiceMan(email: String) {
        val email = email
        val databaseReference = FirebaseDatabase.getInstance().reference.child("serviceMan")

        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Email exists in the serviceMan collection
                        Toast.makeText(this@LoginActivity, "Service man login", Toast.LENGTH_LONG)
                            .show()
                        navigateToServiceManServiceSelection()

                    } else {
                        checkUsersDB(email)                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(
                        this@LoginActivity,
                        "Error checking collection: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun checkUsersDB(email: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Email exists in the serviceMan collection
                        Toast.makeText(this@LoginActivity, "Customer login", Toast.LENGTH_LONG)
                            .show()
                        navigateToUserHomePage()
                       } else {
                        // Email does not exist in the serviceMan collection
                        Toast.makeText(this@LoginActivity, "Not an User", Toast.LENGTH_LONG)
                            .show()
                        navigateToLogin()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(
                        this@LoginActivity,
                        "Error checking collection: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }


//    private fun navigateToAdminHomePage() {
//        val intent = Intent(this, AdminHomePageActivity::class.java)
//        startActivity(intent)
//        finish() // Close the current activity
//    }

    private fun navigateToServiceManServiceSelection() {
        val intent = Intent(this, ServiceManHomePageActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun navigateToUserHomePage() {
        val intent = Intent(this, UserHomePageActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            // User is authenticated and email is verified
            val email = editTextLoginEmail.text.toString().trim()
            // Check if the user is a service man
            checkServiceMan(email)
            finish() // Close the current activity
        } else {
            // User is not authenticated or email is not verified
            Toast.makeText(this, "Authentication failed: Email not verified, check Inbox and verify ", Toast.LENGTH_SHORT)
                .show()
            // You might prompt the user to verify their email here
        }
    }
}
