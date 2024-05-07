package com.project.mad

import ServiceTypeAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class DescriptionPage : AppCompatActivity(),ServiceTypeAdapter.CartCountListener  {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var imagetop: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description_page)

        databaseReference = FirebaseDatabase.getInstance().reference


        val cart = findViewById<ImageView>(R.id.cart)
        cart.setOnClickListener {
            // Define the action to navigate to the homepage
            val intent = Intent(this@DescriptionPage, cartpage::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity
        }

        // Find the ImageView to display the image
        imagetop = findViewById(R.id.imagetop)

        recyclerView = findViewById(R.id.serviceTypeRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        val subcategoryName = intent.getStringExtra("subCategoryName")
        if (subcategoryName != null) {
            val subcategory = findViewById<TextView>(R.id.subcatname)
            subcategory.text = subcategoryName
            fetchAllServiceTypeData(subcategoryName)
        }
       cartCountfn()

    }
    override fun onCartCountUpdated() {
        // Implement the logic to update the cart count here
        cartCountfn()
    }

    private fun cartCountfn() {
        val cartcount = findViewById<TextView>(R.id.cartcount)
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", null)
// Construct a query to find the child nodes with matching userId
        val query = cartReference.orderByChild("userId").equalTo(userId)

// Execute the query
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (cartSnapshot in snapshot.children) {
                    count++
                }
                // Assign the count to the TextView
                cartcount.text = count.toString()
                if (count == 0) {
                    cartcount.visibility = View.GONE
                } else {
                    cartcount.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
            }
        })
    }

    private fun fetchAllServiceTypeData(subcategoryName: String) {
        databaseReference.child("servicetypes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceTypeData = mutableListOf<Quintuple<String, String, String, String, String>>()
                var firstServiceTypeImage: String? = null // Variable to hold the image URL of the first service type

                for (serviceTypeSnapshot in snapshot.children) {
                    val serviceTypeName = serviceTypeSnapshot.child("serviceType").getValue(String::class.java)
                    val serviceTypeImage = serviceTypeSnapshot.child("serviceImage").child("0").getValue(String::class.java)
                    val description = serviceTypeSnapshot.child("description").getValue(String::class.java)
                    val price = serviceTypeSnapshot.child("price").getValue(String::class.java)
                    val subcategory = serviceTypeSnapshot.child("subcategoryName").getValue(String::class.java)
                    val categoryName = serviceTypeSnapshot.child("categoryName").getValue(String::class.java)

                    if (subcategory == subcategoryName) {
                        serviceTypeName?.let { name ->
                            serviceTypeImage?.let { imageUrl ->
                                description?.let { desc ->
                                    price?.let { p ->
                                        categoryName?.let { categoryName ->
                                            val quintuple =
                                                Quintuple(name, desc, p, imageUrl, categoryName)
                                            serviceTypeData.add(quintuple)
                                            // Include description, price, and imageUrl in the data
                                            if (firstServiceTypeImage == null) {
                                                firstServiceTypeImage =
                                                    imageUrl // Save the image URL of the first service type
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Load the image of the first service type into the ImageView
                firstServiceTypeImage?.let {
                    Picasso.get()
                        .load(it)
                        .error(R.drawable.logo) // Placeholder image in case of error
                        .into(imagetop)
                }

                val adapter = ServiceTypeAdapter(serviceTypeData, this@DescriptionPage)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

}
