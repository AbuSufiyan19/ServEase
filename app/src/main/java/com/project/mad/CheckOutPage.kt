package com.project.mad

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CheckOutPage : AppCompatActivity() {

    private lateinit var customername: TextView
    private lateinit var customerphoneno: TextView
    private lateinit var address: TextView
    private lateinit var databaseReference: DatabaseReference

    private var customerNameValue: String? = null
    private var customerPhoneNumberValue: String? = null
    private var addressValue: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_page)

        customername = findViewById(R.id.customername)
        customerphoneno = findViewById(R.id.mobileno)
        address = findViewById(R.id.address)
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", null)
        if (userId != null) {
            checkDatabaseWithId(userId)
        }

        val totalPriceTextView = findViewById<TextView>(R.id.totalamount)
        val serviceNamesTextView = findViewById<TextView>(R.id.services)
        val serviceprovidername = findViewById<TextView>(R.id.serviceprovidername)
        val serviceproviderphoneno = findViewById<TextView>(R.id.serviceproviderphoneno)
        val categoryname = findViewById<TextView>(R.id.categoryName)

        val totalPrice = intent.getStringExtra("totalPrice")
        val categoryName = intent.getStringExtra("categoryName")
        val serviceNames = intent.getStringArrayListExtra("serviceNames")
        val serviceProviderName = intent.getStringExtra("username")
        val serviceProviderPhoneNumber = intent.getStringExtra("phoneNumber")

        totalPriceTextView.text = "Rs $totalPrice (COS Only)"
        serviceNamesTextView.text = "${serviceNames?.joinToString(", ")}"
        serviceprovidername.text = "$serviceProviderName"
        serviceproviderphoneno.text = "$serviceProviderPhoneNumber"
        categoryname.text = "$categoryName"


        val bookNowButton = findViewById<Button>(R.id.booknow)
        var isProcessing = false // flag to check if booking is currently processing

        bookNowButton.setOnClickListener {
            if (!isProcessing) {
                isProcessing = true
                // Show toast message
                Toast.makeText(this, "Your booking is processing", Toast.LENGTH_SHORT).show()

                // Disable the button for 2 seconds
                bookNowButton.isEnabled = false
                android.os.Handler().postDelayed({
                    bookNowButton.isEnabled = true
                    isProcessing = false
                }, 2000)

                if (customerNameValue != null && customerPhoneNumberValue != null && addressValue != null) {
                    saveBooking(customerNameValue!!, customerPhoneNumberValue!!, addressValue!!, categoryName!!, serviceNames!!, serviceProviderName!!, serviceProviderPhoneNumber!!, totalPrice!!)
                } else {
                    // Handle case where customer details are not fetched
                }
            } else {
                Toast.makeText(this, "Please wait, your previous booking is still processing", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkDatabaseWithId(userId: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        customerNameValue = user.username
                        customerPhoneNumberValue = user.phoneNumber
                        val latitude = user.location?.latitude
                        val longitude = user.location?.longitude

                        customername.text = customerNameValue
                        customerphoneno.text = customerPhoneNumberValue

                        if (latitude != null && longitude != null) {
                            addressValue = getAddressFromLatLong(latitude, longitude)
                            address.text = addressValue
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun saveBooking(customerName: String, customerPhoneNumber: String, address: String, categoryName: String, serviceNames: List<String>, serviceProviderName: String, serviceProviderPhoneNumber: String, totalPrice: String) {
        generateBookingId { bookingId ->
            if (bookingId != null) {
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userToken", null)
                val currentDateAndTime = getCurrentDateAndTime()
                val spId = intent.getStringExtra("spid")

                val bookingDetails = hashMapOf(
                    "bookingId" to bookingId,
                    "customerId" to userId,
                    "customerName" to customerName,
                    "customerPhoneNumber" to customerPhoneNumber,
                    "address" to address,
                    "categoryName" to categoryName,
                    "servicesBooked" to serviceNames,
                    "serviceProviderId" to spId,
                    "serviceProviderName" to serviceProviderName,
                    "serviceProviderPhoneNumber" to serviceProviderPhoneNumber,
                    "totalAmount" to totalPrice,
                    "bookingDateTime" to currentDateAndTime,
                    "status" to "booked"
                )

                databaseReference.child(bookingId).setValue(bookingDetails)
                    .addOnSuccessListener {
                        println("Booking details added with ID: $bookingId")
                        if (userId != null) {
                            deleteCartItemsByUserId(userId)
//                            requestSmsPermission(serviceProviderPhoneNumber)
                            sendNotification("Your Booking has Successfully Placed", userId)
//                            if (spId != null) {
//                                sendNotificationToServiceProvider("You have a new booking. Please check your ServEase app for details.", spId)
//                            }

                        }
                        // Handle success
                    }
                    .addOnFailureListener { e ->
                        println("Error adding booking details: $e")
                        // Handle failure
                    }
            }
        }
    }

//    private fun sendNotificationToServiceProvider(message: String, serviceProviderId: Any) {
//        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        val storedUserId = sharedPreferences.getString("userToken", null)
//
//        if (storedUserId == serviceProviderId) {
//            // Build and send the notification
//            val notificationId = 1
//            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.logoapp) // Set your notification icon
//                .setContentTitle("ServEase")
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//
//            // Show the notification
//            with(NotificationManagerCompat.from(this)) {
//                try {
//                    // notificationId is a unique int for each notification that you must define
//                    notify(notificationId, builder.build())
//                    Log.d(TAG, "Notification sent successfully")
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error sending notification: ${e.message}")
//                    e.printStackTrace()
//                }
//            }
//        } else {
//            Log.d(TAG, "User ID doesn't match the stored user ID in SharedPreferences")
//            // Optionally handle the case where the userId doesn't match the one stored in SharedPreferences
//        }
//    }

    private fun sendNotification(message: String, userId: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val storedUserId = sharedPreferences.getString("userToken", null)

        if (storedUserId == userId) {
            // Build and send the notification
            val notificationId = 1
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logoapp) // Set your notification icon
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            // Show the notification
            with(NotificationManagerCompat.from(this)) {
                try {
                    // notificationId is a unique int for each notification that you must define
                    notify(notificationId, builder.build())
                    Log.d(TAG, "Notification sent successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending notification: ${e.message}")
                    e.printStackTrace()
                }
            }
        } else {
            Log.d(TAG, "User ID doesn't match the stored user ID in SharedPreferences")
            // Optionally handle the case where the userId doesn't match the one stored in SharedPreferences
        }
    }


    companion object {
        private const val TAG = "Booking"
        private const val CHANNEL_ID = "100"
    }
    private fun generateBookingId(completion: (String?) -> Unit) {
        val bookingCounterRef = FirebaseDatabase.getInstance().reference.child("bookingCounter")

        bookingCounterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                var counter = mutableData.getValue(Int::class.java) ?: 1
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val bookingId = "SE${year}${String.format("%03d", counter)}"
                counter++
                mutableData.value = counter
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (committed && dataSnapshot != null) {
                    val counter = dataSnapshot.value as Long
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    val bookingId = "SE${year}${String.format("%03d", counter)}"
                    completion(bookingId)
                } else {
                    completion(null)
                }
            }
        })
    }



//    private val SEND_SMS_PERMISSION_REQUEST_CODE = 123
//    private var serviceProviderPhoneNumber: String? = null
//
//    // Request SEND_SMS permission if not granted
//    private fun requestSmsPermission(phoneNumber: String) {
//        serviceProviderPhoneNumber = phoneNumber // Store the phone number
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
//        } else {
//            // Permission already granted, send SMS
//            sendBookingNotification(phoneNumber)
//        }
//    }

    // Handle permission request result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, send SMS
//                serviceProviderPhoneNumber?.let { sendBookingNotification(it) }
//            } else {
//                // Permission denied, show a message or handle it gracefully
//                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun sendBookingNotification(serviceProviderPhoneNumber: String) {
//        val message = "You have a new booking. Please check your Servease app for details."
//        val smsManager = SmsManager.getDefault()
//        smsManager.sendTextMessage(serviceProviderPhoneNumber, null, message, null, null)
//    }

    @SuppressLint("WeekBasedYear")
    private fun getCurrentDateAndTime(): String {
        val sdf = SimpleDateFormat("dd-MM-YYYY HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun deleteCartItemsByUserId(userId: String) {
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")

        // Add a ValueEventListener to fetch the data under "cart"
        cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (cartItemSnapshot in dataSnapshot.children) {
                    // Check if the cart item has a userId matching the one provided
                    val userIdInCart = cartItemSnapshot.child("userId").getValue(String::class.java)
                    if (userIdInCart == userId) {
                        // Found a cart item belonging to the user, delete it
                        cartItemSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                println("Cart item deleted for user: $userId")
                                displayBookingSuccessDialog(this@CheckOutPage)
                                // Handle success
                            }
                            .addOnFailureListener { e ->
                                println("Error deleting cart item for user $userId: $e")
                                // Handle failure
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error accessing cart items: $databaseError")
                // Handle error
            }
        })
    }
    private fun displayBookingSuccessDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Booking Successful")
        alertDialogBuilder.setMessage("Your booking has been successfully placed.")
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            redirectToHomepage(context)
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun redirectToHomepage(context: Context) {
        val intent = Intent(context, UserHomePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }



    private fun getAddressFromLatLong(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addressStr = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val fetchedAddress: Address = addresses[0]
                val stringBuilder = StringBuilder()
                for (i in 0..fetchedAddress.maxAddressLineIndex) {
                    stringBuilder.append(fetchedAddress.getAddressLine(i)).append("\n")
                }
                addressStr = stringBuilder.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressStr
    }

    data class User(
        val username: String? = null,
        val phoneNumber: String? = null,
        val location: Location? = null
    )

    data class Location(
        val latitude: Double? = null,
        val longitude: Double? = null
    )
}
