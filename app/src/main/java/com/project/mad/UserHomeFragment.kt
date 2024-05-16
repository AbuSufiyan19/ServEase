package com.project.mad

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale


@Suppress("DEPRECATION")
class UserHomeFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var recyclerView: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private lateinit var cart: ImageView
    private lateinit var cartCountTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        cart = view.findViewById(R.id.cart)
        cart.setOnClickListener {
            val intent = Intent(requireContext(), cartpage::class.java)
            startActivity(intent)
        }


        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // Fetch all category data from Firebase Database and display in RecyclerView
        fetchAllCategoryData()

        // Initialize locationTextView after inflating the layout
        locationTextView = view.findViewById(R.id.location)
        cartCountTextView = view.findViewById(R.id.cartcount)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchCurrentLocation()
        isnotificationenabled()
        locationTextView.setOnClickListener{
            fetchCurrentLocation()
        }
        // Get reference to the searchView
            val searchView = view.findViewById<SearchView>(R.id.searchView)

        // Set up a text change listener on the searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Filter the adapter data based on the search query
                (recyclerView.adapter as? CategoryAdapter)?.filter(newText)
                return true
            }
        })


//        val cartcount = view.findViewById<TextView>(R.id.cartcount)
//        val cartReference = FirebaseDatabase.getInstance().getReference("cart")
//
//        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        val userId = sharedPreferences.getString("userToken", null)
//// Construct a query to find the child nodes with matching userId
//        val query = cartReference.orderByChild("userId").equalTo(userId)
//
//// Execute the query
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                var count = 0
//                for (cartSnapshot in snapshot.children) {
//                    count++
//                }
//                // Assign the count to the TextView
//                cartcount.text = count.toString()
//                if (count == 0) {
//                    cartcount.visibility = View.GONE
//                } else {
//                    cartcount.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle any errors
//            }
//        })
        updateCartCount()
        return view
    }

    private fun isnotificationenabled() {
        if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
            // Notification permission enabled, proceed with usual operation
            fetchCurrentLocation()
        } else {
            // Notification permission not enabled, prompt the user to enable it
            val dialog = AlertDialog.Builder(requireContext()).apply {
//                setTitle("Permission Required")
                setMessage("Please enable notifications to receive updates.")
                setPositiveButton("Settings") { _, _ ->
                    // Open app settings to allow the user to enable notifications
                    val intent = Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                    startActivity(intent)
                }
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            }.create()

// Set title text color
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                dialog.window?.setBackgroundDrawableResource(android.R.color.white)

                // Set message text color to black
                dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))

            }

            dialog.show()

        }
    }

    override fun onResume() {
        super.onResume()
        updateCartCount()
    }
    private fun updateCartCount() {
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", null)
        val query = cartReference.orderByChild("userId").equalTo(userId)


        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (cartSnapshot in snapshot.children) {
                    count++
                }
                cartCountTextView.text = count.toString()
                cartCountTextView.visibility = if (count == 0) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
            }
        })
    }


    private fun fetchAllCategoryData() {
        databaseReference.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryData = mutableListOf<Pair<String, String>>()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("categoryName").getValue(String::class.java)
                    val imageUrl = categorySnapshot.child("categoryImage").child("0").getValue(String::class.java)
                    categoryName?.let { name ->
                        imageUrl?.let { url ->
                            categoryData.add(Pair(name, url))
                        }
                    }
                }

                // Create adapter and set it to RecyclerView
                val adapter = CategoryAdapter(categoryData)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchCurrentLocation() {
        if (context?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val city = addresses[0].locality ?: ""
                                val adminarea = addresses[0].adminArea ?: ""
//                                val thoroughfare = addresses[0].thoroughfare ?: ""
                                val fulladdress = addresses[0].getAddressLine(0)


                                val completeAddress = "$city, $adminarea"
                                locationTextView.text = completeAddress
                                updateLocationInDatabase(location.latitude, location.longitude, fulladdress)
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


    private fun updateLocationInDatabase(latitude: Double, longitude: Double, fulladdress: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val currentUserRef = databaseReference.child(userId).child("location")
            val locationMap = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "FullAddress" to fulladdress
            )
            currentUserRef.setValue(locationMap)
                .addOnSuccessListener {
                    Log.d("LocationUpdate", "Location updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("LocationUpdate", "Error updating location: ${e.message}", e)
                }
        } else {
            Log.e("LocationUpdate", "Current user ID is null")
        }
    }


    @Deprecated("Deprecated in Java")
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

}
