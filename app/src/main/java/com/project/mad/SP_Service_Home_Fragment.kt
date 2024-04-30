package com.project.mad

import SP_AcceptReject_Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SP_Service_Home_Fragment : Fragment() {
    private lateinit var RecyclerView: RecyclerView
    private lateinit var adapter: SP_AcceptReject_Adapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private lateinit var serviceprovidername: TextView

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var availabilityTextView: TextView // Move the declaration here



    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sp_service_home, container, false)
        RecyclerView = view.findViewById(R.id.RecyclerView)
        RecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)

        locationTextView = view.findViewById(R.id.location)
        availabilityTextView = view.findViewById(R.id.availability) // Initialize here
        serviceprovidername = view.findViewById(R.id.spname)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchCurrentLocation()
        locationTextView.setOnClickListener{
            fetchCurrentLocation()
        }
        fetchAllBookings(getUserIdFromSharedPreferences())
        checkAvailability(getUserIdFromSharedPreferences(),view)

        val availabilitySwitch = view.findViewById<Switch>(R.id.availabilityswitch)
        availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateavailability("Available")
                availabilityTextView.text = "Available"
            } else {
                updateavailability("Notavailable")
                availabilityTextView.text = "Unavailable"
            }
        }
        return view
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun checkAvailability(userId: String, view: View?) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        // Query the users collection to check if the provided userId exists
        val availabilitySwitch = view?.findViewById<Switch>(R.id.availabilityswitch)
        val availabilityTextView = view?.findViewById<TextView>(R.id.availability)

        databaseReference.child("serviceMan").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val availability = dataSnapshot.child("availability").getValue(String::class.java)
                        val spname = dataSnapshot.child("username").getValue(String::class.java)
                        val hello = "Hello, "
                        val serviceProviderNameText = hello + spname
                        serviceprovidername.text = serviceProviderNameText

                        // Set the switch to checked state if availability is "available"
                        availabilitySwitch?.isChecked = availability == "Available"
                        // Assign "available" to TextView if the switch is checked
                        availabilityTextView?.text = if (availabilitySwitch?.isChecked == true) "Available" else "Unavailable"


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


    private fun getUserIdFromSharedPreferences(): String {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userToken", "") ?: ""
    }

    private fun fetchAllBookings(spId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("bookings")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookingData = mutableListOf<Triple<String, String, List<String>>>()

                for (bookingSnapshot in snapshot.children) {
                    val serviceProviderId =
                        bookingSnapshot.child("serviceProviderId").getValue(String::class.java)
                    val status = bookingSnapshot.child("status").getValue(String::class.java)

                    if (spId == serviceProviderId && status == "booked") {
                        val customerName =
                            bookingSnapshot.child("customerName").getValue(String::class.java)
                        val bookingId =
                            bookingSnapshot.child("bookingId").getValue(String::class.java)
                        val servicesBooked = mutableListOf<String>()

                        // Fetch the list of services booked
                        for (serviceSnapshot in bookingSnapshot.child("servicesBooked").children) {
                            val service = serviceSnapshot.getValue(String::class.java) ?: ""
                            servicesBooked.add(service)
                        }

                        val bookingTriple =
                            Triple(customerName ?: "", bookingId ?: "", servicesBooked)
                        bookingData.add(bookingTriple)
                    }
                }
                if (bookingData.isEmpty()) {
                    // If there are no services, hide RecyclerView and show "No Services" TextView
                    RecyclerView.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.noservices)?.visibility = View.VISIBLE

                } else {
                    // If there are services, show RecyclerView and hide "No Services" TextView
                    RecyclerView.visibility = View.VISIBLE
                    view?.findViewById<TextView>(R.id.noservices)?.visibility = View.GONE

                    // Update RecyclerView with the fetched data
                    adapter = SP_AcceptReject_Adapter(bookingData, requireContext())
                    RecyclerView.adapter = adapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    fun reloadBookings(spId: String) {
        fetchAllBookings(spId)
    }
    private fun fetchCurrentLocation() {
        if (context?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val city = addresses[0].locality ?: ""
                                val thoroughfare = addresses[0].thoroughfare ?: ""

                                locationTextView.text = city
                                updateLocationInDatabase(
                                    location.latitude,
                                    location.longitude,
                                    city
                                )
                            } else {
                                Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error getting address: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Location not found, Turn On Location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun updateLocationInDatabase(latitude: Double, longitude: Double, city: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("serviceMan")
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val currentUserRef = databaseReference.child(userId).child("location")
            val locationMap = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
            currentUserRef.setValue(locationMap)
                .addOnSuccessListener {
                    Log.d("LocationUpdate", "Location updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("LocationUpdate", "Error updating location: ${e.message}", e)
                }
            // Store city
            currentUserRef.child("city").setValue(city)
                .addOnSuccessListener {
                    Log.d("LocationUpdate", "City updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("LocationUpdate", "Error updating city: ${e.message}", e)
                }
        } else {
            Log.e("LocationUpdate", "Current user ID is null")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateavailability(s: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("serviceMan")
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val currentUserRef = databaseReference.child(userId)
            val availableMap = hashMapOf(
                "availability" to s
            )
            currentUserRef.updateChildren(availableMap as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("availability", "availability updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("availability", "Error updating availability: ${e.message}", e)
                }

        }
    }

}
