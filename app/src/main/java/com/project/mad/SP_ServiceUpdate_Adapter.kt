package com.project.mad

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SP_ServiceUpdate_Adapter(private val categoryData: List<Pair<String, String>>) : RecyclerView.Adapter<SP_ServiceUpdate_Adapter.CategoryViewHolder>() {

    // Filtered data list
    private var filteredCategoryData: MutableList<Pair<String, String>> = mutableListOf()

    init {
        // Initialize the filtered data list with the original data
        filteredCategoryData.addAll(categoryData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_item_serviceupdate, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val (categoryName, imageUrl) = filteredCategoryData[position]
        holder.bind(categoryName, imageUrl)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SP_ServiceUpdate_Confirm::class.java)
            intent.putExtra("categoryName", categoryName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return filteredCategoryData.size
    }

    // Function to filter data based on search query
    fun filter(query: String) {
        filteredCategoryData.clear()
        if (query.isEmpty()) {
            filteredCategoryData.addAll(categoryData)
        } else {
            val filteredList = categoryData.filter { it.first.contains(query, true) }
            filteredCategoryData.addAll(filteredList)
        }
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView)

        fun bind(categoryName: String, imageUrl: String) {
            categoryNameTextView.text = categoryName
            Picasso.get()
                .load(imageUrl)
                .error(R.drawable.logo) // Placeholder image in case of error
                .into(categoryImageView)
        }
    }
}
