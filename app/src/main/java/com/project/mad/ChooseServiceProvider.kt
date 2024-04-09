package com.project.mad
import ChooseServiceProviderAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ChooseServiceProvider : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_service_provider)

        listView = findViewById(R.id.listView)

        val totalPrice = intent.getStringExtra("totalPrice")
        val categoryName = intent.getStringExtra("categoryName")
        val serviceNames = intent.getStringArrayListExtra("serviceNames")


        val checkout = findViewById<Button>(R.id.checkout)
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
            }
        }


        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("serviceMan")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val serviceMenList = mutableListOf<ServiceMan>()
                for (snapshot in dataSnapshot.children) {
                    val serviceMan = snapshot.getValue(ServiceMan::class.java)
                    serviceMan?.let {
                        val services = it.services?.split(",")?.map { it.trim() } ?: emptyList()
                        if (categoryName in services) {
                            serviceMenList.add(it)
                        }
                    }
                }
                val adapter = ChooseServiceProviderAdapter(this@ChooseServiceProvider, serviceMenList)
                listView.adapter = adapter
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    data class ServiceMan(
        val userId: String? = null,
        val email: String? = null,
        val phoneNumber: String? = null,
        val rating: String? = null,
        val services: String? = null,
        val username: String? = null
    )
}
