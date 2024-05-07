package com.project.mad


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.*


class SP_ServiceUpdate_Confirm : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp_service_update_confirm)

        databaseReference = FirebaseDatabase.getInstance().reference

        listView = findViewById(R.id.listView)
        val categoryName = intent.getStringExtra("categoryName")

        val text = findViewById<TextView>(R.id.catname)
        text.text=categoryName
        if (categoryName != null) {
            val catname = findViewById<TextView>(R.id.catname)
            catname.text = categoryName
            fetchAllSubCategoryData(categoryName)
        }

        val handler = Handler()
        handler.postDelayed({
            // Set click listener for confirm button
            val confirmButton = findViewById<Button>(R.id.confirmButton)
            confirmButton.setOnClickListener {
                storeSelectedServices(categoryName)
            }

            // Set click listener for cancel button
            val cancelButton = findViewById<Button>(R.id.cancelButton)
            cancelButton.setOnClickListener {
                removeSelectedService(categoryName)
            }
        }, 600) // Delay in milliseconds (1.5 seconds)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getString("userToken", "")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this@SP_ServiceUpdate_Confirm, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

// Reference to the specific user's `serviceman` collection
        val userServicemanRef = FirebaseDatabase.getInstance().reference
            .child("serviceMan")
            .child(userId)

        userServicemanRef.child("services").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get existing services of the user if any
                val existingServices = dataSnapshot.getValue(String::class.java) ?: ""

// Split existing services into individual services
                val servicesList = existingServices.split(",")

// Check if categoryName exists among the services
                if (categoryName in servicesList) {
                    // Category exists in the user's services
                    // Make the cancel service button visible
                    findViewById<Button>(R.id.cancelButton).visibility = View.VISIBLE
                    // Make the confirm button invisible
                    findViewById<Button>(R.id.confirmButton).visibility = View.INVISIBLE
                } else {
                    // Category doesn't exist in the user's services
                    // Make the confirm button visible
                    findViewById<Button>(R.id.confirmButton).visibility = View.VISIBLE
                    // Make the cancel service button invisible
                    findViewById<Button>(R.id.cancelButton).visibility = View.INVISIBLE
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(this@SP_ServiceUpdate_Confirm, "Failed to fetch services: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    // Function to store selected services
    private fun storeSelectedServices(categoryName: String?) {
        // Retrieve the user ID from shared preferences
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getString("userToken", "")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this@SP_ServiceUpdate_Confirm, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the specific user's `serviceman` collection
        val userServicemanRef = FirebaseDatabase.getInstance().reference
            .child("serviceMan")
            .child(userId)

        userServicemanRef.child("services").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get existing services of the user if any
                val existingServices = dataSnapshot.getValue(String::class.java) ?: ""

                // Combine existing services with the new one
                val newServices = if (existingServices.isEmpty()) {
                    categoryName ?: ""
                } else {
                    "$existingServices,$categoryName"
                }

                // Update the services for the user
                userServicemanRef.child("services").setValue(newServices)
                    .addOnSuccessListener {
                        Toast.makeText(this@SP_ServiceUpdate_Confirm, "Service updated successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SP_ServiceUpdate_Confirm, ServiceManHomePageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this@SP_ServiceUpdate_Confirm, "Failed to update services", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@SP_ServiceUpdate_Confirm, "Failed to update services: ${databaseError.message}", Toast.LENGTH_SHORT).show()

                // Handle error
            }
        })
    }

    // Function to remove selected service
    private fun removeSelectedService(categoryName: String?) {
        // Retrieve the user ID from shared preferences
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getString("userToken", "")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this@SP_ServiceUpdate_Confirm, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the specific user's `serviceman` collection
        val userServicemanRef = FirebaseDatabase.getInstance().reference
            .child("serviceMan")
            .child(userId)

        userServicemanRef.child("services").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get existing services of the user if any
                val existingServices = dataSnapshot.getValue(String::class.java) ?: ""

                // Remove the selected service from existing services
                val updatedServices = existingServices.split(",").toMutableList()
                updatedServices.remove(categoryName)

                // Update the services for the user
                userServicemanRef.child("services").setValue(updatedServices.joinToString(","))
                    .addOnSuccessListener {
                        Toast.makeText(this@SP_ServiceUpdate_Confirm, "Service removed successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SP_ServiceUpdate_Confirm, ServiceManHomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@SP_ServiceUpdate_Confirm, "Failed to remove service", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@SP_ServiceUpdate_Confirm, "Failed to remove service: ${databaseError.message}", Toast.LENGTH_SHORT).show()

                // Handle error
            }
        })
    }


    private fun fetchAllSubCategoryData(categoryName: String) {
        databaseReference.child("subcategories").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subCategoryData = mutableListOf<String>()

                for (subCategorySnapshot in snapshot.children) {
                    val subCategoryName = subCategorySnapshot.child("subcategoryName").getValue(String::class.java)
                    val category = subCategorySnapshot.child("categoryName").getValue(String::class.java)

                    if (category == categoryName) {
                        subCategoryName?.let {
                            subCategoryData.add(it)
                        }
                    }
                }

                val adapter = object : ArrayAdapter<String>(
                    this@SP_ServiceUpdate_Confirm,
                    R.layout.sp_subcategory_list, // Replace android.R.layout.sp_subcategory_list with your custom layout resource
                    subCategoryData
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        var view = convertView

                        if (view == null) {
                            val inflater = layoutInflater // Use activity's layout inflater directly
                            view = inflater.inflate(R.layout.sp_subcategory_list, parent, false) // Use parent instead of null for the root view
                        }

                        val subCategoryNameTextView = view?.findViewById<TextView>(R.id.subCategoryNameTextView)
                        subCategoryNameTextView?.text = getItem(position)
                        subCategoryNameTextView?.setTextColor(Color.parseColor("#00308f")) // Set text color to black
                        subCategoryNameTextView?.setTypeface(ResourcesCompat.getFont(context, R.font.poppins_light))
                        // Set background color to blue
                        view?.setBackgroundColor(Color.parseColor("#F0F8FF"))

                        return view!!
                    }

            }
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@SP_ServiceUpdate_Confirm, "Fetch Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
