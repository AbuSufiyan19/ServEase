import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.mad.R
import com.project.mad.cartpage
import com.squareup.picasso.Picasso

class CartAdapter(private val cartItems: MutableList<cartpage.Quadrable<String, String, String, String>>, private val context: Context) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var deletedItemPrice: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_layout, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]

        holder.serviceNameTextView.text = currentItem.first
        holder.servicePriceTextView.text = currentItem.second
        holder.categoryNameTextView.text = currentItem.fourth

        // Load image using Picasso
        Picasso.get()
            .load(currentItem.third)
            .error(R.drawable.logo) // Error image if loading fails
            .into(holder.serviceImageView)

        // Set click listener for the delete icon
        holder.deleteIconImageView.setOnClickListener {
            // Remove the item from the cartItems list
            val deletedItem = cartItems.removeAt(position)
            // Notify adapter that data set has changed
            notifyDataSetChanged()
            // Reduce the deleted item's price from the total price
            deletedItemPrice = deletedItem.second.toDouble()
            // Update the total price TextView
            updateTotalPriceTextView()
            // Perform deletion from Firebase
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("userToken", null)
            if (userId != null) {
                deleteServiceFromFirebase(currentItem.first, userId)
            }
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceNameTextView: TextView = itemView.findViewById(R.id.serviceNameTextView)
        val servicePriceTextView: TextView = itemView.findViewById(R.id.servicePriceTextView)
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val serviceImageView: ImageView = itemView.findViewById(R.id.serviceImageView)
        val deleteIconImageView: ImageView = itemView.findViewById(R.id.deletesingle)
    }

    private fun deleteServiceFromFirebase(serviceName: String, userId: String) {
        val cartReference = FirebaseDatabase.getInstance().getReference("cart")

        // Construct a query to find the child nodes with matching userId and serviceName
        val query = cartReference.orderByChild("userId").equalTo(userId)

        // Execute the query
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (cartSnapshot in snapshot.children) {
                    val cartUserId = cartSnapshot.child("userId").getValue(String::class.java)
                    val cartServiceName = cartSnapshot.child("serviceName").getValue(String::class.java)

                    // Check if userId and serviceName match the given criteria
                    if (cartUserId == userId && cartServiceName == serviceName) {
                        // Delete the child node from Firebase
                        cartSnapshot.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    @SuppressLint("CutPasteId")
    private fun updateTotalPriceTextView() {
        val priceTextView = (context as cartpage).findViewById<TextView>(R.id.totalprice)
        val currentTotal = priceTextView.text.toString().toDouble()
        val newTotal = currentTotal - deletedItemPrice
        priceTextView.text = newTotal.toString()
        // Check if the new total price is zero

        // Check if the new total price is zero
        if (newTotal == 0.0) {
            // Set background image when total price is zero
            val recyclerView = (context as cartpage).findViewById<RecyclerView>(R.id.cartRecyclerView)
            recyclerView.setBackgroundResource(R.drawable.emptycart)

            // Hide the chooseserviceprovider button
            val chooseserviceprovider = (context as cartpage).findViewById<Button>(R.id.chooseserviceprovider)
            val tot = (context as cartpage).findViewById<TextView>(R.id.total)
            val totalpc = (context as cartpage).findViewById<TextView>(R.id.totalprice)
            chooseserviceprovider.visibility = View.GONE
            tot.visibility = View.GONE
            totalpc.visibility = View.GONE
        } else {
            // If new total is not zero, show the chooseserviceprovider button
            val chooseserviceprovider = (context as cartpage).findViewById<Button>(R.id.chooseserviceprovider)
            val tot = (context as cartpage).findViewById<TextView>(R.id.total)
            val totalpc = (context as cartpage).findViewById<TextView>(R.id.totalprice)
            chooseserviceprovider.visibility = View.VISIBLE
            tot.visibility = View.VISIBLE
            totalpc.visibility = View.VISIBLE
        }
    }
}
