package com.project.mad

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Ratings : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: EditText
    private lateinit var submitReviewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_sheet_layout)

        ratingBar = findViewById(R.id.ratingBar)
        reviewEditText = findViewById(R.id.reviewEditText)
        submitReviewButton = findViewById(R.id.submitReviewButton)

        submitReviewButton.setOnClickListener {
            val rating = ratingBar.rating
            val review = reviewEditText.text.toString()

            // Display toast message
            val message = "Your Ratings & review is submitted"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            // Clear the review EditText for the next entry
            reviewEditText.text.clear()

            // Reset the rating bar
            ratingBar.rating = 0.0f
        }
    }
}
