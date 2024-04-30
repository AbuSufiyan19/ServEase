import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.mad.DescriptionPage
import com.project.mad.R

class ServiceTypeAdapter(private val serviceTypeData: List<DescriptionPage.Quintuple<String, String, String, String, String>>
, private val cartCountListener: CartCountListener
) : RecyclerView.Adapter<ServiceTypeAdapter.ServiceTypeViewHolder>(), Filterable {

    private var filteredData: MutableList<DescriptionPage.Quintuple<String, String, String, String, String>> = serviceTypeData.toMutableList()
    private var originalData: MutableList<DescriptionPage.Quintuple<String, String, String, String, String>> = serviceTypeData.toMutableList()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("cart")

    interface CartCountListener {
        fun onCartCountUpdated()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_type, parent, false)
        return ServiceTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceTypeViewHolder, position: Int) {
        val (serviceTypeName, description, price, imageUrl, categoryName) = filteredData[position]
        holder.bind(serviceTypeName, description, price, imageUrl, categoryName)
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    inner class ServiceTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceTypeNameTextView: TextView = itemView.findViewById(R.id.servicetypename)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        private val priceTextView: TextView = itemView.findViewById(R.id.price)
        private val addtocartButton: Button = itemView.findViewById(R.id.addtocart)

        fun bind(serviceTypeName: String, description: String, price: String, imageUrl: String, categoryName: String) {
            serviceTypeNameTextView.text = serviceTypeName
            descriptionTextView.text = description
            priceTextView.text = price
            addtocartButton.setOnClickListener {
                val userId = getUserIdFromSharedPreferences(itemView.context)

                // Check if the user exists in the cart
                val isUserInCart = databaseReference
                    .orderByChild("userId")
                    .equalTo(userId)

                isUserInCart.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User exists in the cart
                            val userCartItems = dataSnapshot.children.toList()

                            // Check if the cart is empty for this user
                            if (userCartItems.isEmpty()) {
                                // Cart is empty, add the service with its category name
                                val cartItem = mapOf(
                                    "userId" to userId,
                                    "serviceName" to serviceTypeName,
                                    "servicePrice" to price,
                                    "serviceImageUrl" to imageUrl, // Add your image URL here
                                    "categoryName" to categoryName
                                )
                                databaseReference.push().setValue(cartItem)
                                Toast.makeText(itemView.context, "Service added to cart", Toast.LENGTH_SHORT).show()
                                cartCountListener.onCartCountUpdated()

                            } else {
                                // Get the category name from the first item in the cart
                                val currentCategoryName = userCartItems[0].child("categoryName").getValue(String::class.java)

                                // Check if the service category name matches the category name in the cart
                                if (currentCategoryName != null && currentCategoryName == categoryName) {
                                    // Check if the service already exists in the cart for this user
                                    val isServiceInCart = userCartItems.any { cartItem ->
                                        val serviceName = cartItem.child("serviceName").getValue(String::class.java)
                                        serviceName == serviceTypeName
                                    }

                                    if (isServiceInCart) {
                                        // Service already exists in the cart for this user, display a toast message
                                        Toast.makeText(itemView.context, "Service already in cart", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Service does not exist in the cart for this user, add it
                                        val cartItem = mapOf(
                                            "userId" to userId,
                                            "serviceName" to serviceTypeName,
                                            "servicePrice" to price,
                                            "serviceImageUrl" to imageUrl, // Add your image URL here
                                            "categoryName" to categoryName
                                        )
                                        databaseReference.push().setValue(cartItem)
                                        Toast.makeText(itemView.context, "Service added to cart", Toast.LENGTH_SHORT).show()
                                        cartCountListener.onCartCountUpdated()

                                    }
                                } else {
                                    // Category name does not match, display a toast message
                                    Toast.makeText(itemView.context, "All services must belong to the same category", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // User does not exist in the cart, add the user along with the service
                            val cartItem = mapOf(
                                "userId" to userId,
                                "serviceName" to serviceTypeName,
                                "servicePrice" to price,
                                "serviceImageUrl" to imageUrl, // Add your image URL here
                                "categoryName" to categoryName
                            )
                            databaseReference.push().setValue(cartItem)
                            Toast.makeText(itemView.context, "Service added to cart", Toast.LENGTH_SHORT).show()
                            cartCountListener.onCartCountUpdated()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle onCancelled
                    }
                })
                cartCountListener.onCartCountUpdated()
            }
        }



        private fun getUserIdFromSharedPreferences(context: Context): String? {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("userToken", null)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().toLowerCase().trim()
                filteredData = if (query.isEmpty()) {
                    originalData
                } else {
                    originalData.filter { (serviceTypeName, _, _, _, _) ->
                        serviceTypeName.toLowerCase().contains(query)
                    }.toMutableList()
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredData = results?.values as? MutableList<DescriptionPage.Quintuple<String, String, String, String, String>> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun updateOriginalData(newData: List<DescriptionPage.Quintuple<String, String, String, String, String>>) {
        originalData = newData.toMutableList()
        notifyDataSetChanged()
    }
}
