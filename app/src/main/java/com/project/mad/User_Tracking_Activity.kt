package com.project.mad

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class User_Tracking_Activity : AppCompatActivity() {
    private lateinit var acceptRejectDateTime: String
    private lateinit var arrivedDateTime: String
    private lateinit var startedDateTime: String
    private lateinit var finishedDateTime: String
    private lateinit var paymentrequestedDateTime: String
    private lateinit var paymentcompletedDateTime: String
    private lateinit var cancelledDateTime: String

    private lateinit var address: String
    private lateinit var bookingDateTime: String
    private lateinit var categoryName: String
    private lateinit var customerId: String
    private lateinit var customerName: String
    private lateinit var serviceProviderId: String
    private lateinit var serviceProviderName: String
    private lateinit var serviceProviderPhoneNumber: String
    private lateinit var status: String
    private lateinit var totalAmount: String
    private lateinit var BookingID: String
    private lateinit var rating: String

    private lateinit var acc_rej0: TextView
    private lateinit var default_point0: ImageView
    private lateinit var datetime0: TextView
    private lateinit var progressBar0: ProgressBar


    private lateinit var acc_rej: TextView
    private lateinit var datetime1: TextView
    private lateinit var default_point1: ImageView

    private lateinit var sp_arrive: TextView
    private lateinit var datetime2: TextView
    private lateinit var default_point2: ImageView
    private lateinit var progressBar1: ProgressBar

    private lateinit var work_started: TextView
    private lateinit var datetime3: TextView
    private lateinit var default_point3: ImageView
    private lateinit var progressBar2: ProgressBar

    private lateinit var work_finished: TextView
    private lateinit var datetime4: TextView
    private lateinit var default_point4: ImageView
    private lateinit var progressBar3: ProgressBar

    private lateinit var payment: TextView
    private lateinit var datetime5: TextView
    private lateinit var default_point5: ImageView
    private lateinit var buttonpaid: Button
    private lateinit var progressBar4: ProgressBar

    private lateinit var ratings: TextView
    private lateinit var buttoncancel: Button
    private lateinit var query: TextView



    companion object {
        private const val REQUEST_CALL_PERMISSION = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_tracking)

        acc_rej0 = findViewById(R.id.acc_rej0)
        default_point0 = findViewById(R.id.default_point0)
        datetime0 = findViewById(R.id.datetime0)
        progressBar0 = findViewById(R.id.prg0)


        acc_rej = findViewById(R.id.acc_rej)
        datetime1 = findViewById(R.id.datetime1)
        default_point1 = findViewById(R.id.default_point1)

        sp_arrive = findViewById(R.id.sp_arrive)
        datetime2 = findViewById(R.id.datetime2)
        default_point2 = findViewById(R.id.default_point2)
        progressBar1 = findViewById(R.id.prg1)

        work_started = findViewById(R.id.work_started)
        datetime3 = findViewById(R.id.datetime3)
        default_point3 = findViewById(R.id.default_point3)
        progressBar2 = findViewById(R.id.prg2)

        work_finished = findViewById(R.id.work_finished)
        datetime4 = findViewById(R.id.datetime4)
        default_point4 = findViewById(R.id.default_point4)
        progressBar3 = findViewById(R.id.prg3)

        payment = findViewById(R.id.payment)
        datetime5 = findViewById(R.id.datetime5)
        default_point5 = findViewById(R.id.default_point5)
        progressBar4 = findViewById(R.id.prg4)
        buttonpaid = findViewById(R.id.buttonpayment)

        ratings = findViewById(R.id.ratings)
        query = findViewById(R.id.query)
        buttoncancel = findViewById(R.id.buttoncancel)
        val serviceProviderPhoneNumber = intent.getStringExtra("serviceProviderPhoneNumber")

        val bookingId = intent.getStringExtra("bookingId")
        val orderid = findViewById<TextView>(R.id.orderid)
        orderid.text = bookingId

        query.setOnClickListener{
            val intent = Intent(this@User_Tracking_Activity, UserQueryActivity::class.java)
            intent.putExtra("bookingId", bookingId) // Pass bookingId
            startActivity(intent)
            finish()
        }
        val call = findViewById<ImageView>(R.id.call)
        val callphone = findViewById<TextView>(R.id.callphone)
        callphone.setOnClickListener{
            callphonefn()
        }
        call.setOnClickListener {
            callphonefn()
        }
        buttonpaid.visibility = View.GONE
        ratings.visibility = View.GONE
        fetchDataandUpdateUI(bookingId)
//
        buttonpaid.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "paymentdone",
                "paymentcompletedDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@User_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@User_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)
        }
        buttoncancel.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "cancelled",
                "cancelledDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@User_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@User_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)
        }
        val reviewRatings: TextView = findViewById(R.id.ratings)

        reviewRatings.setOnClickListener {
            val view: View = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(view)
            dialog.show()

            // Handle submit button click inside the bottom sheet dialog
            val submitButton = view.findViewById<Button>(R.id.submitReviewButton)
            val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
            val reviewEditText = view.findViewById<EditText>(R.id.reviewEditText)

            submitButton.setOnClickListener {
                val rating = ratingBar.rating
                val review = reviewEditText.text.toString()


                if (rating > 0 || review.isNotBlank()) {
                    // Get a reference to the booking node in the database
                    val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

                    // Update the rating in the database
                    val updateData = mapOf<String, Any>(
                        "rating" to rating.toString(), // Convert rating to string
                        "review" to review
                        // Add other fields if needed
                    )

                    // Update the database with the new rating
                    bookingRef.updateChildren(updateData)
                        .addOnSuccessListener {
                            // Show a toast message indicating that the rating has been submitted
                            Toast.makeText(this, "Your rating of $rating has been submitted", Toast.LENGTH_SHORT).show()

                            // Clear the review EditText for the next entry
                            reviewEditText.text.clear()

                            // Reset the rating bar
                            ratingBar.rating = 0.0f

                            // Dismiss the bottom sheet dialog after submission
                            dialog.dismiss()

                            // Update rating under serviceProviderId in serviceMan collection
                            val serviceProviderRatingRef = FirebaseDatabase.getInstance().reference
                                .child("serviceMan")
                                .child(serviceProviderId) // Replace serviceProviderId with the actual ID of the service provider
                                .child("rating")

                            serviceProviderRatingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // Check if the rating exists in the database
                                    if (dataSnapshot.exists()) {
                                        val existingRatingStr = dataSnapshot.getValue(String::class.java)
                                        val existingRating = existingRatingStr?.toDoubleOrNull() ?: 0.0
                                        val newRating: Double
                                        if (existingRating > 0.0) {
                                            newRating = (existingRating + rating) / 2 // Calculate average rating
                                        } else {
                                            newRating = rating.toDouble()
                                        }
                                        val roundedRating = String.format("%.1f", newRating).toDouble()

                                        // Update the rating in the database
                                        serviceProviderRatingRef.setValue(roundedRating.toString())
                                            .addOnSuccessListener {
                                                // Rating updated successfully
                                                // Handle success if needed
                                                val intent = Intent(this@User_Tracking_Activity, UserHomePageActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                // Failed to update the rating
                                                // Handle failure if needed
                                            }
                                    } else {
                                        // Handle case where rating does not exist in the database
                                        // This could be the first rating for the service provider
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle error
                                }
                            })


                        }
                        .addOnFailureListener {
                            // Show a toast message indicating the failure to update the rating
                            Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Display a message indicating that a rating is required
                    Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun callphonefn() {
        if (serviceProviderPhoneNumber.isNotEmpty()) {
            // Check if the app has permission to make phone calls
            if (ContextCompat.checkSelfPermission(this@User_Tracking_Activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with making the call
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$serviceProviderPhoneNumber")
                startActivity(intent)
            } else {
                // Permission is not granted, request it from the user
                ActivityCompat.requestPermissions(this@User_Tracking_Activity, arrayOf(Manifest.permission.CALL_PHONE),
                    SP_Tracking_Activity.REQUEST_CALL_PERMISSION
                )
            }
        } else {
            // Handle case where phone number is not available or empty
            Toast.makeText(this@User_Tracking_Activity, "Phone number not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SP_Tracking_Activity.REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with making the call
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$serviceProviderPhoneNumber")
                startActivity(intent)
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(this@User_Tracking_Activity, "Permission denied. Cannot make a call.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDataandUpdateUI(bookingId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

        // Read data from the specified bookingId
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the data for the bookingId
                    acceptRejectDateTime = dataSnapshot.child("acceptrejectdatetime").getValue(String::class.java)?: ""
                    arrivedDateTime = dataSnapshot.child("arrivedDateTime").getValue(String::class.java)?: ""
                    startedDateTime = dataSnapshot.child("startedDateTime").getValue(String::class.java)?: ""
                    finishedDateTime = dataSnapshot.child("finishedDateTime").getValue(String::class.java)?: ""
                    paymentrequestedDateTime = dataSnapshot.child("paymentrequestedDateTime").getValue(String::class.java)?: ""
                    paymentcompletedDateTime = dataSnapshot.child("paymentcompletedDateTime").getValue(String::class.java)?: ""
                    cancelledDateTime = dataSnapshot.child("cancelledDateTime").getValue(String::class.java)?: ""




                    address = dataSnapshot.child("address").getValue(String::class.java)?: ""
                    BookingID = dataSnapshot.child("bookingId").getValue(String::class.java)?: ""
                    bookingDateTime = dataSnapshot.child("bookingDateTime").getValue(String::class.java)?: ""
                    categoryName = dataSnapshot.child("categoryName").getValue(String::class.java)?: ""
                    customerId = dataSnapshot.child("customerId").getValue(String::class.java)?: ""
                    customerName = dataSnapshot.child("customerName").getValue(String::class.java)?: ""
                    serviceProviderId = dataSnapshot.child("serviceProviderId").getValue(String::class.java)?: ""
                    serviceProviderName = dataSnapshot.child("serviceProviderName").getValue(String::class.java)?: ""
                    serviceProviderPhoneNumber = dataSnapshot.child("serviceProviderPhoneNumber").getValue(String::class.java)?: ""
                    status = dataSnapshot.child("status").getValue(String::class.java)?: ""
                    totalAmount = dataSnapshot.child("totalAmount").getValue(String::class.java)?: ""


                    val sp_name = findViewById<TextView>(R.id.sp_name)
                    sp_name.text = serviceProviderName
                    // Update UI with the retrieved data
                    // You can set this data to your TextViews or use it as needed
                    if(status=="accepted"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        buttoncancel.visibility = View.VISIBLE

                    }
                    else if(status=="arrived"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Service Provider Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        buttoncancel.visibility = View.GONE

                    }
                    else if(status=="started"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Service Provider Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_started.text = "Service has Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        buttoncancel.visibility = View.GONE

                    }
                    else if(status=="finished"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Service Provider Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_started.text = "Service has Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Service has Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        buttoncancel.visibility = View.GONE

                    }
                    else if(status=="paymentrequested"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Work Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        payment.text = "Payment Requested"
                        buttonpaid.visibility = View.VISIBLE
                        datetime5.text = paymentrequestedDateTime

                        buttoncancel.visibility = View.GONE

                    }
                    else if(status=="paymentdone"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Work Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))

                        payment.text = "Payment Done"
                        datetime5.text = paymentcompletedDateTime
                        default_point5.setImageResource(R.drawable.bg_circle_done)
                        progressBar4.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@User_Tracking_Activity, R.color.blue1))
                        buttonpaid.visibility = View.GONE
                        buttoncancel.visibility = View.GONE


                        // Check if the booking has a rating and review in the database
                        val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)
                        bookingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val hasRating = dataSnapshot.child("rating").exists()
                                    val hasReview = dataSnapshot.child("review").exists()

                                    // If the booking does not have a rating and review, make the rating view visible
                                    if (!hasRating && !hasReview) {
                                        ratings.visibility = View.VISIBLE
                                    } else {
                                        // If the booking has a rating and review, make the rating view invisible
                                        ratings.visibility = View.GONE
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle error
                            }
                        })
                    }
                    else if(status=="booked"){
                        datetime0.text = bookingDateTime

//                        acc_rej.text = "Service Booked"
//                            datetime1.text = bookingDateTime
//                            default_point1.setImageResource(R.drawable.bg_circle_done)

                            buttonpaid.visibility = View.GONE
                            ratings.visibility = View.GONE
                        buttoncancel.visibility = View.VISIBLE

                    }
                    else if(status == "cancelled"){
                        acc_rej.text = "Service Cancelled"
                        datetime1.text = cancelledDateTime

                        datetime0.text = bookingDateTime
                        val params = datetime1.layoutParams as ViewGroup.MarginLayoutParams
                        params.leftMargin = 105 * resources.displayMetrics.density.toInt() // Convert dp to pixels
                        datetime1.layoutParams = params
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.visibility = View.GONE
                        datetime2.visibility = View.GONE
                        default_point2.visibility = View.GONE
                        progressBar1.visibility = View.GONE

                        work_started.visibility = View.GONE
                        datetime3.visibility = View.GONE
                        default_point3.visibility = View.GONE
                        progressBar2.visibility = View.GONE

                        work_finished.visibility = View.GONE
                        datetime4.visibility = View.GONE
                        default_point4.visibility = View.GONE
                        progressBar3.visibility = View.GONE

                        payment.visibility = View.GONE
                        datetime5.visibility = View.GONE
                        default_point5.visibility = View.GONE
                        progressBar4.visibility = View.GONE
                        buttoncancel.visibility = View.GONE

                    }
                    else{
                        acc_rej.text = "Service Rejected"
                        datetime0.text = bookingDateTime

                        datetime1.text = acceptRejectDateTime
                        val params = datetime1.layoutParams as ViewGroup.MarginLayoutParams
                        params.leftMargin = 105 * resources.displayMetrics.density.toInt() // Convert dp to pixels
                        datetime1.layoutParams = params
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.visibility = View.GONE
                        datetime2.visibility = View.GONE
                        default_point2.visibility = View.GONE
                        progressBar1.visibility = View.GONE

                        work_started.visibility = View.GONE
                        datetime3.visibility = View.GONE
                        default_point3.visibility = View.GONE
                        progressBar2.visibility = View.GONE

                        work_finished.visibility = View.GONE
                        datetime4.visibility = View.GONE
                        default_point4.visibility = View.GONE
                        progressBar3.visibility = View.GONE

                        payment.visibility = View.GONE
                        datetime5.visibility = View.GONE
                        default_point5.visibility = View.GONE
                        progressBar4.visibility = View.GONE
                        buttoncancel.visibility = View.GONE

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun getCurrentDateAndTime(): String {
        val sdf = SimpleDateFormat("dd-MM-YYYY HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

}