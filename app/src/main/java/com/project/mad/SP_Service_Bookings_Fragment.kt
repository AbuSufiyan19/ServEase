package com.project.mad

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SP_Service_Bookings_Fragment : Fragment(), SPBookingsAdapter.OnItemClickListener{
    private lateinit var listViewBookings: ListView
    private lateinit var SPbookingsAdapter: SPBookingsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var searchView: SearchView
    private lateinit var nobookings: TextView

    private var originalBookingsList: MutableList<SP_Service_Bookings_Fragment.Booking> = mutableListOf()


    @SuppressLint("MissingInflatedId")
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
        nobookings = view.findViewById(R.id.nobookings)
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
                    val bookingId = bookingSnapshot.child("bookingId").getValue(String::class.java)
                    val customerPhoneNumber = bookingSnapshot.child("customerPhoneNumber").getValue(String::class.java)
                    val serviceProviderId = bookingSnapshot.child("serviceProviderId").getValue(String::class.java)
                    val datetime = bookingSnapshot.child("bookingDateTime").getValue(String::class.java)
                    val servicesBooked = mutableListOf<String>()
                    for (serviceSnapshot in bookingSnapshot.child("servicesBooked").children) {
                        val service = serviceSnapshot.getValue(String::class.java) ?: ""
                        servicesBooked.add(service)
                    }
                    val categoryName = bookingSnapshot.child("categoryName").getValue(String::class.java)
                    val status = bookingSnapshot.child("status").getValue(String::class.java)
                    if (serviceProviderId != null && servicesBooked.isNotEmpty() && serviceProviderId == userId) {
                        fetchCategoryImage(categoryName, servicesBooked, status, datetime, bookingId, customerPhoneNumber)
                    }
                }
//                // Update visibility based on the bookings list
//                if (originalBookingsList.isEmpty()) {
//                    nobookings.visibility = View.VISIBLE
//                    listViewBookings.visibility = View.GONE
//                    searchView.visibility = View.GONE
//                } else {
//                    nobookings.visibility = View.GONE
//                    listViewBookings.visibility = View.VISIBLE
//                    searchView.visibility = View.VISIBLE
//                    updateListView(originalBookingsList)
//                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error fetching user bookings: $databaseError")
            }
        })
    }

    private fun fetchCategoryImage(
        categoryName: String?,
        servicesBooked: List<String>,
        status: String?,
        datetime: String?,
        bookingId: String?,
        customerPhoneNumber: String?
    ) {
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
                                datetime,
                                bookingId,
                                customerPhoneNumber
                            )
                            originalBookingsList.add(booking)
                        }
                    }
                    // Update visibility based on the bookings list
                    if (originalBookingsList.isEmpty()) {
                        nobookings.visibility = View.VISIBLE
                        listViewBookings.visibility = View.GONE
                        searchView.visibility = View.GONE
                    } else {
                        nobookings.visibility = View.GONE
                        listViewBookings.visibility = View.VISIBLE
                        searchView.visibility = View.VISIBLE
                        updateListView(originalBookingsList)
                    }
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
//    override fun onItemClick(booking: SP_Service_Bookings_Fragment.Booking) {
//        // Handle item click here, for example, show a toast with booking details
//        Toast.makeText(requireContext(), "Clicked on booking with ID: ${booking.datetime}", Toast.LENGTH_SHORT).show()
//    }
    override fun onItemClick(booking: Booking) {
        // Handle item click here, navigate to the tracking activity
        val intent = Intent(requireContext(), SP_Tracking_Activity::class.java)
        // Pass any necessary data to the intent
        intent.putExtra("bookingId", booking.bookingId)
        intent.putExtra("customerPhoneNumber", booking.customerPhoneNumber)
        startActivity(intent)
    }

    private fun updateListView(bookingsList: MutableList<SP_Service_Bookings_Fragment.Booking>) {
        SPbookingsAdapter = SPBookingsAdapter(requireContext(), bookingsList)
        SPbookingsAdapter.setOnItemClickListener(this) // Set the click listener
        listViewBookings.adapter = SPbookingsAdapter
    }

    data class Booking(
        val servicesBooked: List<String>,
        val status: String?,
        val categoryName: String?,
        val imageUrl: String?,
        val datetime: String?,
        val bookingId: String?,
        val customerPhoneNumber: String?
    )
}