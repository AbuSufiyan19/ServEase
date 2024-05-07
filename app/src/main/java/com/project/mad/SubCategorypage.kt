package com.project.mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.google.firebase.database.*

class SubCategorypage : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_categorypage)

        databaseReference = FirebaseDatabase.getInstance().reference


        val cart = findViewById<ImageView>(R.id.cart)
        cart.setOnClickListener {
            // Define the action to navigate to the homepage
            val intent = Intent(this@SubCategorypage, cartpage::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity
        }
        recyclerView = findViewById(R.id.subCategoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        val categoryName = intent.getStringExtra("categoryName")
        if (categoryName != null) {
            val catname=findViewById<TextView>(R.id.catname)
            catname.text=categoryName
            fetchAllSubCategoryData(categoryName)
        }

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val adapter = recyclerView.adapter
                if (adapter is SubCategoryAdapter) {
                    adapter.filter(newText)
                }
                return true
            }
        })
        cartCountfn()
    }
    override fun onResume() {
        super.onResume()
        cartCountfn() // Refresh cart count when activity resumes
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

    private fun fetchAllSubCategoryData(categoryName: String) {
        databaseReference.child("subcategories").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subCategoryData = mutableListOf<Pair<String, String>>()

                for (subCategorySnapshot in snapshot.children) {
                    val subCategoryName = subCategorySnapshot.child("subcategoryName").getValue(String::class.java)
                    val subCategoryImage = subCategorySnapshot.child("subcategoryImage").child("0").getValue(String::class.java)
                    val category = subCategorySnapshot.child("categoryName").getValue(String::class.java)

                    if (category == categoryName) {
                        subCategoryName?.let { name ->
                            subCategoryImage?.let { imageUrl ->
                                subCategoryData.add(Pair(name, imageUrl))
                            }
                        }
                    }
                }

                val adapter = SubCategoryAdapter(subCategoryData)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("SubCategorypage", "Database error: ${error.message}")
                Toast.makeText(this@SubCategorypage, "Failed to retrieve subcategories. Please try again later.", Toast.LENGTH_SHORT).show()


            }
        })
    }
}


