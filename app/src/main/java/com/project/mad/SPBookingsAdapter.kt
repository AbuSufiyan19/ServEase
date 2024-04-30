package com.project.mad
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.project.mad.SP_Service_Bookings_Fragment.Booking
import java.text.SimpleDateFormat
import java.util.*

class SPBookingsAdapter(
    context: Context,
    bookingsList: MutableList<Booking>
) : ArrayAdapter<Booking>(context, 0, bookingsList.sortedByDescending { parseDateTime(it.datetime) }) {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.sp_list_item_bookings, parent, false)
        }

        val currentBooking = getItem(position)

        val status = itemView?.findViewById<TextView>(R.id.status)
        val textViewServicesBooked = itemView?.findViewById<TextView>(R.id.textViewServicesBooked)
        val imageview = itemView?.findViewById<ImageView>(R.id.imageviewCategory)
        val datetime = itemView?.findViewById<TextView>(R.id.datetime)

        if (currentBooking?.status == "paymentdone") {
            // If true, set the text to "Status: Service Done"
            status?.text = "Status: Service Done"
        } else {
            status?.text = "Status: ${currentBooking?.status}"
        }
        datetime?.text = "${currentBooking?.datetime}"
        textViewServicesBooked?.text = "${currentBooking?.servicesBooked?.joinToString()}"
        currentBooking?.imageUrl?.let { imageUrl ->
            Picasso.get().load(imageUrl).into(imageview)
        }

        // Set background color based on position
        val bgColor = if (position % 2 == 0) R.color.white else R.color.grey0
        itemView?.setBackgroundColor(ContextCompat.getColor(context, bgColor))

        // Set click listener
        itemView?.setOnClickListener {
            onItemClickListener?.onItemClick(currentBooking!!)
        }

        return itemView!!
    }

    interface OnItemClickListener {
        fun onItemClick(booking: Booking)
    }


}
private fun parseDateTime(dateTimeString: String?): Date {
    val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    return format.parse(dateTimeString ?: "") ?: Date()
}
