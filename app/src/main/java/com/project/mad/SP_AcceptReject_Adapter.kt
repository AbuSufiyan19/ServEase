import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.mad.R
import com.project.mad.ServiceManHomePageActivity
import com.project.mad.UserHomePageActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SP_AcceptReject_Adapter(
    private var bookingData: List<Triple<String, String, List<String>>>,
    private val context: Context
) : RecyclerView.Adapter<SP_AcceptReject_Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerNameTextView: TextView = itemView.findViewById(R.id.customerNameTextView)
        val servicesBookedTextView: TextView = itemView.findViewById(R.id.servicesBookedTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sp_item_acceptreject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookingData[position]

        holder.customerNameTextView.text = booking.first
        holder.servicesBookedTextView.text = booking.third.joinToString(", ") // Convert list to string

        holder.acceptButton.setOnClickListener {
            val bookingId = booking.second // Retrieve the booking ID for the clicked item

            // Perform a database operation to update the status to "accepted" for the booking ID
            updateBookingStatus(bookingId, "accepted")
        }

        holder.rejectButton.setOnClickListener {
            val bookingId = booking.second // Retrieve the booking ID for the clicked item

            // Perform a database operation to update the status to "accepted" for the booking ID
            updateBookingStatus(bookingId, "rejected")
        }
    }
    private fun updateBookingStatus(bookingId: String, status: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("bookings").child(bookingId)

        val acceptRejectDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(
            Date()
        )

        val bookingUpdateMap = HashMap<String, Any>()
        bookingUpdateMap["status"] = status
        bookingUpdateMap["acceptrejectdatetime"] = acceptRejectDateTime

        databaseReference.updateChildren(bookingUpdateMap)
            .addOnSuccessListener {
                showToast(if (status == "accepted") "Service Accepted" else "Service Rejected")
                val intent = Intent(context, ServiceManHomePageActivity::class.java)
                context.startActivity(intent)
            }
            .addOnFailureListener {
                showToast("Failed to update booking status.")
            }
    }



    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    override fun getItemCount(): Int {
        return bookingData.size
    }
}




