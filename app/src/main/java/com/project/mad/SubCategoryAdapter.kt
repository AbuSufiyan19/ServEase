package com.project.mad

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import android.widget.Filter
import android.widget.Filterable

class SubCategoryAdapter(private val subCategoryData: List<Pair<String, String>>) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>(), Filterable {

    private var filteredData: List<Pair<String, String>> = subCategoryData

    // Original data to use when filtering
    private var originalData: List<Pair<String, String>> = subCategoryData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sub_category, parent, false)
        return SubCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        val (subCategoryName, imageUrl) = filteredData[position]
        holder.bind(subCategoryName, imageUrl)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DescriptionPage::class.java)
            intent.putExtra("subCategoryName", subCategoryName)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return filteredData.size
    }

    inner class SubCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subCategoryNameTextView: TextView = itemView.findViewById(R.id.subcategoryname)
        private val subCategoryImageView: ImageView = itemView.findViewById(R.id.subcategoryimage)

        fun bind(subCategoryName: String, imageUrl: String) {
            subCategoryNameTextView.text = subCategoryName
            Picasso.get()
                .load(imageUrl)
                .error(R.drawable.logo) // Placeholder image in case of error
                .into(subCategoryImageView)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().toLowerCase().trim()
                filteredData = if (query.isEmpty()) {
                    originalData // If the query is empty, return the original data
                } else {
                    originalData.filter { (subCategoryName, _) ->
                        subCategoryName.toLowerCase().contains(query) // Filter based on subcategory name
                    }
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredData = results?.values as? List<Pair<String, String>> ?: emptyList()
                notifyDataSetChanged() // Notify RecyclerView about the data change
            }
        }
    }

    // Function to trigger filtering
    fun filter(newText: String) {
        val filter = filter
        filter.filter(newText)
    }

    // Function to update original data when needed
    fun updateOriginalData(newData: List<Pair<String, String>>) {
        originalData = newData
        notifyDataSetChanged()
    }
}
