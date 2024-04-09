package com.project.mad
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserBookingFragment : Fragment() {
    private lateinit var listViewBookings: ListView
    private lateinit var bookingsAdapter: BookingAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var searchView: SearchView

    private var originalBookingsList: MutableList<Booking> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_booking, container, false)
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
                    val customerId = bookingSnapshot.child("customerId").getValue(String::class.java)
                    val servicesBooked = mutableListOf<String>()
                    for (serviceSnapshot in bookingSnapshot.child("servicesBooked").children) {
                        val service = serviceSnapshot.getValue(String::class.java) ?: ""
                        servicesBooked.add(service)
                    }
                    val categoryName = bookingSnapshot.child("categoryName").getValue(String::class.java)
                    val bookingDateTime = bookingSnapshot.child("bookingDateTime").getValue(String::class.java)
                    if (customerId != null && servicesBooked.isNotEmpty() && customerId == userId) {
                        fetchCategoryImage(categoryName, servicesBooked, bookingDateTime)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error fetching user bookings: $databaseError")
            }
        })
    }

    private fun fetchCategoryImage(categoryName: String?, servicesBooked: List<String>, bookingDateTime: String?) {
        if (categoryName != null) {
            val categoryRef = FirebaseDatabase.getInstance().getReference("categories")
            val query = categoryRef.orderByChild("categoryName").equalTo(categoryName)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (categorySnapshot in dataSnapshot.children) {
                        val imageUrl = categorySnapshot.child("categoryImage").child("0").getValue(String::class.java)
                        imageUrl?.let {
                            val booking = Booking(
                                servicesBooked,
                                bookingDateTime,
                                categoryName,
                                imageUrl
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

    private fun updateListView(bookingsList: MutableList<Booking>) {
        bookingsAdapter = BookingAdapter(requireContext(), bookingsList)
        listViewBookings.adapter = bookingsAdapter
    }

    data class Booking(
        val servicesBooked: List<String>,
        val bookingDateTime: String?,
        val categoryName: String?,
        val imageUrl: String?
    )
}
