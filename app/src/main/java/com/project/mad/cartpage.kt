package com.project.mad

import CartAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class cartpage : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteAllButton: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartpage)

            fun displayDeleteConfirmationDialog(context: Context) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to delete all services?")
            alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userToken", null)
                if (userId != null) {
                    deleteAllServicesFromFirebase(userId)
                }
            }
            alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        deleteAllButton = findViewById(R.id.deleteall)
        deleteAllButton.setOnClickListener {
            displayDeleteConfirmationDialog(this)
        }

        val chooseserviceprovider = findViewById<Button>(R.id.chooseserviceprovider)
        chooseserviceprovider.setOnClickListener {
            // Get the total price from the TextView
            val totalPrice = findViewById<TextView>(R.id.totalprice).text.toString()

            // Assuming you have a reference to your Firebase database
            val cartReference = FirebaseDatabase.getInstance().getReference("cart")

            // Get the category name from Firebase
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("userToken", null)

            userId?.let { uid ->
                cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var categoryName: String? = null
                        val serviceNames =
                            mutableListOf<String>() // List to store all service names

                        for (cartSnapshot in snapshot.children) {
                            val cartUserId =
                                cartSnapshot.child("userId").getValue(String::class.java)
                            if (cartUserId == uid) {
                                // Retrieve category name from the first matching cart item
                                if (categoryName == null) {
                                    categoryName = cartSnapshot.child("categoryName")
                                        .getValue(String::class.java)
                                }
                                // Retrieve service name from each cart item and add to the list
                                val serviceName =
                                    cartSnapshot.child("serviceName").getValue(String::class.java)
                                serviceName?.let {
                                    serviceNames.add(it)
                                }
                            }
                        }

                        // Create an intent to navigate to the ChooseServiceProvider activity
                        val intent =
                            Intent(this@cartpage, ChooseServiceProvider::class.java).apply {
                                // Pass the total price, category name, and service names as extras in the intent
                                putExtra("totalPrice", totalPrice)
                                putExtra("categoryName", categoryName)
                                putStringArrayListExtra("serviceNames", ArrayList(serviceNames))
                            }
                        startActivity(intent)
                        finish() // Optional: Close the current activity
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }
        }

        databaseReference = FirebaseDatabase.getInstance().reference
        recyclerView = findViewById(R.id.cartRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        fetchAllUserCartData()
    }

    private fun deleteAllServicesFromFirebase(userId: String) {
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")

        // Construct a query to find the child nodes with matching userId
        val query = cartReference.orderByChild("userId").equalTo(userId)

        // Execute the query
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (cartSnapshot in snapshot.children) {
                    // Delete each child node from Firebase
                    cartSnapshot.ref.removeValue()
                }
                // Reload the page after successful deletion
                fetchAllUserCartData()

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun fetchAllUserCartData() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", null)

        // Assuming you have a reference to your Firebase database
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")

        userId?.let { uid ->
            cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cartServiceData = mutableListOf<Quadrable<String, String, String, String>>()
                    var totalPrice = 0.0 // Variable to store the total price


                    for (cartSnapshot in snapshot.children) {
                        val cartUserId = cartSnapshot.child("userId").getValue(String::class.java)
                        if (cartUserId == uid) {
                            // Retrieve service details from the cart
                            val serviceName = cartSnapshot.child("serviceName").getValue(String::class.java)
                            val servicePrice = cartSnapshot.child("servicePrice").getValue(String::class.java)
                            val serviceImageUrl = cartSnapshot.child("serviceImageUrl").getValue(String::class.java)
                            val categoryName = cartSnapshot.child("categoryName").getValue(String::class.java)

                            // Add service details to the list
                            serviceName?.let { name ->
                                servicePrice?.let { price ->
                                    serviceImageUrl?.let { imageUrl ->
                                        categoryName?.let { category ->
                                            val quad = Quadrable(name, price, imageUrl, category)
                                            cartServiceData.add(quad)
                                            totalPrice += price.toDouble() // Add price to total
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pass the cart service data to the adapter
                    val adapter = CartAdapter(cartServiceData, this@cartpage)
                    recyclerView.adapter = adapter
                    // Check if cartServiceData is empty

                    if (cartServiceData.isEmpty()) {
                        // Set background image when cart is empty
                        recyclerView.setBackgroundResource(R.drawable.emptycart)
                        totalPrice = 0.0 // Reset total price to 0 when cart is empty
                    }
                    val priceTextView = findViewById<TextView>(R.id.totalprice)
                    // Set the total price to the TextView
                    priceTextView.text = totalPrice.toString()
                    val chooseserviceprovider = findViewById<Button>(R.id.chooseserviceprovider)
                    val tot = findViewById<TextView>(R.id.total)
                    val totalpc = findViewById<TextView>(R.id.totalprice)
                    val summary = findViewById<TextView>(R.id.summary)
                    if (totalPrice == 0.0) {
                        chooseserviceprovider.visibility = View.GONE
                        tot.visibility = View.GONE
                        summary.visibility = View.GONE
                        totalpc.visibility = View.GONE
                        deleteAllButton.visibility = View.GONE
                    } else {
                        chooseserviceprovider.visibility = View.VISIBLE
                        tot.visibility = View.VISIBLE
                        summary.visibility = View.VISIBLE
                        totalpc.visibility = View.VISIBLE
                        deleteAllButton.visibility = View.VISIBLE

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }


    data class Quadrable<T, U, V, W>(
        val first: T,
        val second: U,
        val third: V,
        val fourth: W
    )

}

