package com.project.mad

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextLoginEmail: EditText
    private lateinit var editTextLoginPassword: EditText
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var buttonLogin: Button
    private lateinit var textViewSignUp: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        createNotificationChannel()

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("users") // "users" is the node in the Realtime Database

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail)
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignUp = findViewById(R.id.textViewSignUp)

        forgotPasswordTextView = findViewById(R.id.forgotpassword)
        // Set OnClickListener for the TextView
        forgotPasswordTextView.setOnClickListener {
            val email = editTextLoginEmail.text.toString().trim()
            if (validateEmail(email) && email.isNotEmpty()) {
                // Show confirmation dialog
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null)
                val dialogBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                val dialog = dialogBuilder.create()
                dialog.show()

                // Set up click listeners for buttons
                dialogView.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
                    dialog.dismiss() // Dismiss dialog if Cancel is clicked
                }

                dialogView.findViewById<Button>(R.id.buttonOk).setOnClickListener {
                    dialog.dismiss() // Dismiss dialog if OK is clicked
                    sendPasswordResetEmail(email) // Send password reset email
                }
            } else {
                Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show()
            }
        }


        // Add FocusChangeListener for email field
        editTextLoginEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = editTextLoginEmail.text.toString().trim()
                if (!validateEmail(email)) {
                    editTextLoginEmail.error = "Enter a valid email address"
                } else {
                    editTextLoginEmail.error = null
                }
            }
        }

        // Add FocusChangeListener for password field
        editTextLoginPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = editTextLoginPassword.text.toString().trim()
                if (password.isEmpty()) {
                    editTextLoginPassword.error = "Password cannot be empty"
                } else {
                    editTextLoginPassword.error = null
                }
            }
        }

        buttonLogin.setOnClickListener {
            val email = editTextLoginEmail.text.toString().trim()
            val password = editTextLoginPassword.text.toString().trim()

            if (email.isNotEmpty() && validateEmail(email)) {
                if(password.isNotEmpty()) {
                    loginAuthentication(email, password)
                }
                else{
                    Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            }
        }


        textViewSignUp.setOnClickListener {
            // Navigate to SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent successfully", Toast.LENGTH_SHORT).show()
                    // Password reset email sent successfully
                    // You can show a toast or perform any other action here
                } else {
                    Toast.makeText(this, "Password reset email failed to send", Toast.LENGTH_SHORT).show()
                    // Password reset email failed to send
                    // You can show a toast or perform any other action here
                }
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
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Authentication failed: Email not verified, check Inbox and verify",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // If login fails, display a message to the user.
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Login failed: ${task.exception?.message}",
                        Snackbar.LENGTH_LONG
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
        val databaseReference = FirebaseDatabase.getInstance().reference.child("serviceMan")

        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val approvalStatus = data.child("approvalstatus").getValue(String::class.java)
                            if (approvalStatus == "Approved") {
                                // Email exists in the serviceMan collection and approved
//                                Toast.makeText(
//                                    this@LoginActivity,
//                                    "Service man login",
//                                    Toast.LENGTH_LONG
//                                ).show()
                                navigateToServiceManHomePageActivity()
                                return // Exit loop if approved
                            }
                            else if (approvalStatus == "Deactivate") {
                                navigateToAccountBlocked()
                            }
                            else if (approvalStatus == "Proof Uploaded"){
                                Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Your Document will be verified within 3 to 4 hours until then wait patiently",
                                    Snackbar.LENGTH_LONG
                                ).show()                            }
                            else{
                                Toast.makeText(
                                    this@LoginActivity,
                                    "You need to upload your Document",
                                    Toast.LENGTH_LONG
                                ).show()
                                navigateToProofPage()
                                return
                            }
                        }
                        // If loop completes without finding approved status
                        // Handle accordingly, maybe show a message or prevent login
                    } else {
                        // Service man not found
                        checkUsersDB(email)
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

    private val CHANNEL_ID = "100"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Your Channel Name"
            val descriptionText = "Your Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }




    private fun checkUsersDB(email: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Email exists in the serviceMan collection
//                        Toast.makeText(this@LoginActivity, "Customer login", Toast.LENGTH_LONG)
//                            .show()
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

    private fun navigateToAccountBlocked() {
        val intent = Intent(this, SP_Account_Blocked::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }
    private fun navigateToProofPage() {
        val intent = Intent(this, SP_Proof_Submission::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }
    private fun navigateToServiceManHomePageActivity() {
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
        } else {
            // User is not authenticated or email is not verified
            Toast.makeText(this, "Authentication failed: Email not verified, check Inbox and verify ", Toast.LENGTH_SHORT)
                .show()
            // You might prompt the user to verify their email here
        }
    }
}
