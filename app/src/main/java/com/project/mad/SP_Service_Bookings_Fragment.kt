package com.project.mad

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SP_Service_Bookings_Fragment : Fragment() {
    private lateinit var listViewBookings: ListView
    private lateinit var SPbookingsAdapter: SPBookingsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var searchView: SearchView

    private var originalBookingsList: MutableList<SP_Service_Bookings_Fragment.Booking> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sp_service_bookings, container, false)
        listViewBookings = view.findViewById(R.id.listViewBookings)
        databaseReference = FirebaseDatabase.getInstance().reference
        searchView = view.findViewById(R.id.searchView)
        fetchUserBookings(getUserIdFromSharedPreferences())
        setupSearchView()
        return view
    }
    private fun getUserIdFromSharedPreferences(): String {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userToken", "") ?: ""
    }
    private fun fetchUserBookings(userId: String) {
        val bookingsReference = FirebaseDatabase.getInstance().getReference("bookings")

        bookingsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                originalBookingsList.clear()
                for (bookingSnapshot in dataSnapshot.children) {
                    val serviceProviderId = bookingSnapshot.child("serviceProviderId").getValue(String::class.java)
                    val datetime = bookingSnapshot.child("acceptrejectdatetime").getValue(String::class.java)
                    val servicesBooked = mutableListOf<String>()
                    for (serviceSnapshot in bookingSnapshot.child("servicesBooked").children) {
                        val service = serviceSnapshot.getValue(String::class.java) ?: ""
                        servicesBooked.add(service)
                    }
                    val categoryName = bookingSnapshot.child("categoryName").getValue(String::class.java)
                    val status = bookingSnapshot.child("status").getValue(String::class.java)
                    if (serviceProviderId != null && servicesBooked.isNotEmpty() && serviceProviderId == userId) {
                        fetchCategoryImage(categoryName, servicesBooked, status, datetime)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error fetching user bookings: $databaseError")
            }
        })
    }

    private fun fetchCategoryImage(categoryName: String?, servicesBooked: List<String>, status: String?, datetime: String?) {
        if (categoryName != null) {
            val categoryRef = FirebaseDatabase.getInstance().getReference("categories")
            val query = categoryRef.orderByChild("categoryName").equalTo(categoryName)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (categorySnapshot in dataSnapshot.children) {
                        val imageUrl = categorySnapshot.child("categoryImage").child("0").getValue(String::class.java)
                        imageUrl?.let {
                            val booking = SP_Service_Bookings_Fragment.Booking(
                                servicesBooked,
                                status,
                                categoryName,
                                imageUrl,
                                datetime
                            )
                            originalBookingsList.add(booking)
                        }
                    }
                    // Initialize adapter with the populated list
                    updateListView(originalBookingsList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error fetching category image: $databaseError")
                }
            })
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    updateListView(originalBookingsList)
                } else {
                    val filteredList = originalBookingsList.filter {
                        it.servicesBooked.any { service -> service.contains(newText, ignoreCase = true) }
                    }
                    updateListView(filteredList.toMutableList())
                }
                return true
            }
        })
    }
    private fun updateListView(bookingsList: MutableList<SP_Service_Bookings_Fragment.Booking>) {
        SPbookingsAdapter = SPBookingsAdapter(requireContext(), bookingsList)
        listViewBookings.adapter = SPbookingsAdapter
    }

    data class Booking(
        val servicesBooked: List<String>,
        val status: String?,
        val categoryName: String?,
        val imageUrl: String?,
        val datetime: String?
    )
}