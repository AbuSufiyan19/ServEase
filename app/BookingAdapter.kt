import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.project.mad.R

class BookingAdapter(context: Context, private val bookings: List<Booking>) :
    ArrayAdapter<Booking>(context, 0, bookings) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_booking, parent, false)
        }

        val currentBooking = bookings[position]

        val textViewBookingDateTime = itemView?.findViewById<TextView>(R.id.textViewBookingDateTime)
        val textViewServicesBooked = itemView?.findViewById<TextView>(R.id.textViewServicesBooked)

        textViewBookingDateTime?.text = "Date and Time: ${currentBooking.bookingDateTime}"
        textViewServicesBooked?.text = "Services Booked: ${currentBooking.serviceNames.joinToString()}"

        return itemView!!
    }
}
