package com.project.mad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class UserQueryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_query)

        // Retrieve bookingId from intent
        val bookingId = intent.getStringExtra("bookingId")

        // Find views
        val grievanceEditText = findViewById<EditText>(R.id.grievences)
        val submitButton = findViewById<Button>(R.id.submit)

        // Set OnClickListener for submit button
        submitButton.setOnClickListener {
            // Retrieve grievance text from EditText
            val grievanceText = grievanceEditText.text.toString()

            // Update grievance data in Firebase
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)
            val updateData = mapOf<String, Any>(
                "grievances" to grievanceText
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@UserQueryActivity, "Grievances submitted successfully", Toast.LENGTH_SHORT).show()

                    // Navigate back to UserHomePageActivity
                    val intent = Intent(this@UserQueryActivity, UserHomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@UserQueryActivity, "Failed to submit grievances: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
