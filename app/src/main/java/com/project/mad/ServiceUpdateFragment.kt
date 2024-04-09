package com.project.mad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.ArrayList

class ServiceUpdateFragment : Fragment(){

    private lateinit var serviceRecyclerView: RecyclerView
    private lateinit var adapter: SP_ServiceUpdate_Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_update, container, false)
        serviceRecyclerView = view.findViewById(R.id.serviceRecyclerView)

        // Initialize adapter here
        adapter = SP_ServiceUpdate_Adapter(ArrayList())

        serviceRecyclerView.adapter = adapter
        serviceRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        fetchAllCategoryData()
        return view
    }


    private fun fetchAllCategoryData() {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("categories")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
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
                val adapter = SP_ServiceUpdate_Adapter(categoryData)
                serviceRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

}