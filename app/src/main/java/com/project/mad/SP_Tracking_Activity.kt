package com.project.mad

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest


class SP_Tracking_Activity : AppCompatActivity() {
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
    private lateinit var review: String
    private lateinit var customerPhoneNumber: String


    private lateinit var acc_rej: TextView
    private lateinit var datetime1: TextView
    private lateinit var default_point1: ImageView

    private lateinit var sp_arrive: TextView
    private lateinit var datetime2: TextView
    private lateinit var default_point2: ImageView
    private lateinit var buttonarrive: Button
    private lateinit var progressBar1: ProgressBar

    private lateinit var work_started: TextView
    private lateinit var datetime3: TextView
    private lateinit var default_point3: ImageView
    private lateinit var buttonstart: Button
    private lateinit var progressBar2: ProgressBar

    private lateinit var work_finished: TextView
    private lateinit var datetime4: TextView
    private lateinit var default_point4: ImageView
    private lateinit var buttonend: Button
    private lateinit var progressBar3: ProgressBar

    private lateinit var payment: TextView
    private lateinit var datetime5: TextView
    private lateinit var default_point5: ImageView
    private lateinit var buttonrequest: Button
    private lateinit var progressBar4: ProgressBar

    private lateinit var ratings: TextView
    private lateinit var reviews: TextView

    private lateinit var acc_rej0: TextView
    private lateinit var default_point0: ImageView
    private lateinit var datetime0: TextView
    private lateinit var progressBar0: ProgressBar


    companion object {
        const val REQUEST_CALL_PERMISSION = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp_tracking)

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
        buttonarrive = findViewById(R.id.buttonarrive)
        progressBar1 = findViewById(R.id.prg1)

        work_started = findViewById(R.id.work_started)
        datetime3 = findViewById(R.id.datetime3)
        default_point3 = findViewById(R.id.default_point3)
        buttonstart = findViewById(R.id.buttonstart)
        progressBar2 = findViewById(R.id.prg2)

        work_finished = findViewById(R.id.work_finished)
        datetime4 = findViewById(R.id.datetime4)
        default_point4 = findViewById(R.id.default_point4)
        buttonend = findViewById(R.id.buttonend)
        progressBar3 = findViewById(R.id.prg3)

        payment = findViewById(R.id.payment)
        datetime5 = findViewById(R.id.datetime5)
        default_point5 = findViewById(R.id.default_point5)
        buttonrequest = findViewById(R.id.buttonrequest)
        progressBar4 = findViewById(R.id.prg4)
        ratings = findViewById(R.id.ratings)
        reviews = findViewById(R.id.reviews)




        val customerPhoneNumberTextView = findViewById<TextView>(R.id.customerphonenumber)
        val orderIdTextView = findViewById<TextView>(R.id.orderid)

        val bookingId = intent.getStringExtra("bookingId")
        customerPhoneNumber = intent.getStringExtra("customerPhoneNumber").toString()

        orderIdTextView.text = bookingId

        val call = findViewById<ImageView>(R.id.call)
        customerPhoneNumberTextView.setOnClickListener{
            callphonefn()
        }
        call.setOnClickListener {
            callphonefn()
        }

// Add this constant at the top of your activity



        // Get a reference to the "bookings" collection in Firebase Realtime Database

        ratings.visibility = View.GONE
        reviews.visibility = View.GONE

        fetchDataandUpdateUI(bookingId)

        buttonarrive.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "arrived",
                "arrivedDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@SP_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@SP_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)
        }

        buttonstart.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "started",
                "startedDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@SP_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@SP_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)

        }
        buttonend.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "finished",
                "finishedDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@SP_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@SP_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)
        }

        buttonrequest.setOnClickListener{
            // Get the current date and time
            val currentDateTime = getCurrentDateAndTime()

            // Get a reference to the booking node in the database
            val bookingRef = FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId!!)

            // Update the status field to "arrived" and the arriveddatetime field to the current date/time
            val updateData = mapOf<String, Any>(
                "status" to "paymentrequested",
                "paymentrequestedDateTime" to currentDateTime
            )
            bookingRef.updateChildren(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this@SP_Tracking_Activity, "Status Updated", Toast.LENGTH_SHORT).show()
                    fetchDataandUpdateUI(bookingId)
                }
                .addOnFailureListener {
                    // Handle the failure to update the status
                    Toast.makeText(this@SP_Tracking_Activity, "Status Update Failed", Toast.LENGTH_SHORT).show()

                }
            fetchDataandUpdateUI(bookingId)
        }


    }

    private fun callphonefn() {
        if (customerPhoneNumber.isNotEmpty()) {
            // Check if the app has permission to make phone calls
            if (ContextCompat.checkSelfPermission(this@SP_Tracking_Activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with making the call
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$customerPhoneNumber")
                startActivity(intent)
            } else {
                // Permission is not granted, request it from the user
                ActivityCompat.requestPermissions(this@SP_Tracking_Activity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
            }
        } else {
            // Handle case where phone number is not available or empty
            Toast.makeText(this@SP_Tracking_Activity, "Phone number not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with making the call
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$customerPhoneNumber")
                startActivity(intent)
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(this@SP_Tracking_Activity, "Permission denied. Cannot make a call.", Toast.LENGTH_SHORT).show()
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
                    rating = dataSnapshot.child("rating").getValue(String::class.java)?: ""
                    review = dataSnapshot.child("review").getValue(String::class.java)?: ""



                    val cs_name = findViewById<TextView>(R.id.cs_name)
                    cs_name.text = customerName
                    // Update UI with the retrieved data
                    // You can set this data to your TextViews or use it as needed
                    if(status=="accepted"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        buttonstart.visibility = View.GONE // Hide the button
                        buttonend.visibility = View.GONE // Hide the button
                        buttonrequest.visibility = View.GONE // Hide the button
                    }
                    else if(status=="arrived"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        buttonarrive.visibility = View.GONE // Hide the button
                        buttonend.visibility = View.GONE // Hide the button
                        buttonrequest.visibility = View.GONE // Hide the button
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))
                        buttonstart.visibility = View.VISIBLE // Hide the button

                    }
                    else if(status=="started"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        buttonstart.visibility = View.GONE // Hide the button
                        buttonrequest.visibility = View.GONE // Hide the button
                        buttonend.visibility = View.VISIBLE
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                    }
                    else if(status=="finished"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        buttonstart.visibility = View.GONE // Hide the button
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Work Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        buttonend.visibility = View.GONE // Hide the button
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))
                        buttonrequest.visibility = View.VISIBLE // Hide the button

                    }
                    else if(status=="paymentrequested"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        buttonstart.visibility = View.GONE // Hide the button
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Work Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        buttonend.visibility = View.GONE // Hide the button
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        payment.text = "Payment Requested"
                        datetime5.text = paymentrequestedDateTime
                        buttonrequest.visibility = View.GONE // Hide the button
                    }
                    else if(status=="paymentdone"){
                        datetime0.text = bookingDateTime

                        acc_rej.text = "Service Accepted"
                        datetime1.text = acceptRejectDateTime
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.text = "Reached Site"
                        datetime2.text = arrivedDateTime
                        default_point2.setImageResource(R.drawable.bg_circle_done)
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_started.text = "Work Started"
                        datetime3.text = startedDateTime
                        default_point3.setImageResource(R.drawable.bg_circle_done)
                        buttonstart.visibility = View.GONE // Hide the button
                        progressBar2.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        work_finished.text = "Work Completed"
                        datetime4.text = finishedDateTime
                        default_point4.setImageResource(R.drawable.bg_circle_done)
                        buttonend.visibility = View.GONE // Hide the button
                        progressBar3.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))

                        payment.text = "Payment Done"
                        datetime5.text = paymentcompletedDateTime
                        buttonrequest.visibility = View.GONE // Hide the button
                        default_point5.setImageResource(R.drawable.bg_circle_done)
                        progressBar4.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SP_Tracking_Activity, R.color.blue1))
                        ratings.visibility = View.VISIBLE
                        ratings.text = "Rating : ${if (rating.isEmpty()) "N/A" else rating}"
                        reviews.visibility = View.VISIBLE
                        reviews.text = "Review : ${if (review.isEmpty()) "N/A" else review}"

                    }
                    else if(status=="booked"){
                        datetime0.text = bookingDateTime
//                        acc_rej.text = "Service Booked"
//                        datetime1.text = bookingDateTime
//                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        buttonarrive.visibility = View.GONE // Hide the button
                        buttonstart.visibility = View.GONE // Hide the button
                        buttonend.visibility = View.GONE // Hide the button
                        buttonrequest.visibility = View.GONE // Hide the button

                    }
                    else if(status == "cancelled"){
                        acc_rej.text = "Service Cancelled"
                        datetime0.text = bookingDateTime
                        datetime1.text = cancelledDateTime

                        val params = datetime1.layoutParams as ViewGroup.MarginLayoutParams
                        params.leftMargin = 105 * resources.displayMetrics.density.toInt() // Convert dp to pixels
                        datetime1.layoutParams = params
                        default_point1.setImageResource(R.drawable.bg_circle_done)

                        sp_arrive.visibility = View.GONE
                        datetime2.visibility = View.GONE
                        default_point2.visibility = View.GONE
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.visibility = View.GONE

                        work_started.visibility = View.GONE
                        datetime3.visibility = View.GONE
                        default_point3.visibility = View.GONE
                        buttonstart.visibility = View.GONE // Hide the button
                        progressBar2.visibility = View.GONE

                        work_finished.visibility = View.GONE
                        datetime4.visibility = View.GONE
                        default_point4.visibility = View.GONE
                        buttonend.visibility = View.GONE // Hide the button
                        progressBar3.visibility = View.GONE

                        payment.visibility = View.GONE
                        datetime5.visibility = View.GONE
                        buttonrequest.visibility = View.GONE // Hide the button
                        default_point5.visibility = View.GONE
                        progressBar4.visibility = View.GONE

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
                        buttonarrive.visibility = View.GONE // Hide the button
                        progressBar1.visibility = View.GONE

                        work_started.visibility = View.GONE
                        datetime3.visibility = View.GONE
                        default_point3.visibility = View.GONE
                        buttonstart.visibility = View.GONE // Hide the button
                        progressBar2.visibility = View.GONE

                        work_finished.visibility = View.GONE
                        datetime4.visibility = View.GONE
                        default_point4.visibility = View.GONE
                        buttonend.visibility = View.GONE // Hide the button
                        progressBar3.visibility = View.GONE

                        payment.visibility = View.GONE
                        datetime5.visibility = View.GONE
                        buttonrequest.visibility = View.GONE // Hide the button
                        default_point5.visibility = View.GONE
                        progressBar4.visibility = View.GONE

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
