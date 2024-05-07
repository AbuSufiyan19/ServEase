package com.project.mad
import ChooseServiceProviderAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlin.math.*


class ChooseServiceProvider : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var noserviceman: TextView
    private lateinit var checkout: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_service_provider)

        listView = findViewById(R.id.listView)

        val totalPrice = intent.getStringExtra("totalPrice")
        val categoryName = intent.getStringExtra("categoryName")
        val serviceNames = intent.getStringArrayListExtra("serviceNames")

        noserviceman = findViewById(R.id.noserviceman)
        noserviceman.visibility = View.GONE

        checkout = findViewById(R.id.checkout)
//        checkout.setOnClickListener {
//            // Define the action to navigate to the homepage
//            val intent = Intent(this@ChooseServiceProvider, CheckOutPage::class.java).apply {
//                putExtra("totalPrice", totalPrice)
//                putStringArrayListExtra("serviceNames", serviceNames?.let { ArrayList(it) })
//            }
//            startActivity(intent)
//            finish() // Optional: Close the current activity
//        }
        checkout.setOnClickListener {
            // Get the selected service man from the adapter
            val selectedServiceMan = (listView.adapter as ChooseServiceProviderAdapter).getSelectedServiceMan()

            // Check if a service provider is selected
            if (selectedServiceMan != null) {
                // Define the action to navigate to the CheckOutPage
                val intent = Intent(this@ChooseServiceProvider, CheckOutPage::class.java).apply {
                    putExtra("totalPrice", totalPrice)
                    putExtra("categoryName",categoryName)
                    putStringArrayListExtra("serviceNames", serviceNames?.let { ArrayList(it) })
                    putExtra("spid", selectedServiceMan.userId)

                    // Pass the selected service provider's details via intent extras
                    putExtra("username", selectedServiceMan.username)
                    putExtra("phoneNumber", selectedServiceMan.phoneNumber)

                }
                startActivity(intent)
                finish() // Optional: Close the current activity
            } else {
                // Display a message to the user indicating that no service provider is selected
                // You can implement this based on your UI/UX requirements
                Toast.makeText(this, "Please choose a service provider", Toast.LENGTH_SHORT).show()
            }
        }

        // Retrieve user token from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", "")

        val databaseReference = FirebaseDatabase.getInstance().reference
        // Query the users collection to check if the provided userId exists
        if (userId != null) {
            databaseReference.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // If the user exists, retrieve its data
                            val userData = dataSnapshot.getValue(User::class.java)
                            val latitude = userData?.location?.latitude ?: 0.0
                            val longitude = userData?.location?.longitude ?: 0.0

                            // Once latitude and longitude are fetched, proceed with filtering service men
                            filterServiceMen(latitude, longitude , categoryName)
                        } else {
                            // Handle the case where the provided userId doesn't exist
                            // For example, display a message indicating user not found
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
        }
    }

    private fun filterServiceMen(
        userLatitude: Double,
        userLongitude: Double,
        categoryName: String?
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("serviceMan")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val serviceMenList = mutableListOf<ServiceMan>()
                for (snapshot in dataSnapshot.children) {
                    val serviceMan = snapshot.getValue(ServiceMan::class.java)
                    serviceMan?.let {
                        val services = it.services?.split(",")?.map { it.trim() } ?: emptyList()
                        val availability = snapshot.child("availability").getValue(String::class.java)
                        val locationSnapshot = snapshot.child("location")
                        val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)

                        // Check if latitude and longitude are not null
                        if (latitude != null && longitude != null) {
                            // Calculate distance between user's coordinates and service man's location
                            val distance = calculateDistance(userLatitude, userLongitude, latitude, longitude)

                            // Check if distance is within 50 kilometers radius and service man is available
                            if (categoryName in services && availability == "Available" && distance <= 50) {
//                                serviceMenList.add(it)
                                val rating = snapshot.child("rating").getValue(String::class.java)
                                serviceMenList.add(it.copy(rating = rating)) // Update the rating field

                            }
                        }
                    }
                }
                if (serviceMenList.isEmpty()) {
                    // No service providers available
                    listView.visibility = View.GONE
                    noserviceman.visibility = View.VISIBLE
                    checkout.visibility = View.GONE
                } else {
                    // Service providers available
                    listView.visibility = View.VISIBLE
                    noserviceman.visibility = View.GONE
                    checkout.visibility = View.VISIBLE


                    // Sort serviceMenList based on ratings (assuming ratings are stored as String)
                    serviceMenList.sortByDescending { it.rating?.toDoubleOrNull() ?: 0.0 }

                    val adapter = ChooseServiceProviderAdapter(this@ChooseServiceProvider, serviceMenList)
                    listView.adapter = adapter
                }
                // Sort serviceMenList based on ratings (assuming ratings are stored as String)
//                serviceMenList.sortByDescending { it.rating?.toDoubleOrNull() ?: 0.0 }
//
//
//                val adapter = ChooseServiceProviderAdapter(this@ChooseServiceProvider, serviceMenList)
//                listView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) + (cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(lonDistance / 2) * sin(lonDistance / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c // Distance in kilometers
    }

    data class ServiceMan(
        val userId: String? = null,
        val email: String? = null,
        val phoneNumber: String? = null,
        val rating: String? = null,
        val services: String? = null,
        val username: String? = null
    )

    data class User(
        val location: Location = Location()
    )
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )
}
