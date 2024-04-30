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
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(context: Context, bookingsList: List<UserBookingFragment.Booking>) :
    ArrayAdapter<UserBookingFragment.Booking>(context, 0, bookingsList.sortedByDescending { it.bookingDateTime?.let { it1 ->
        parseDate(
            it1
        )
    } }) {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_booking, parent, false)
        }

        val currentBooking = getItem(position)

        val textViewBookingDateTime = itemView?.findViewById<TextView>(R.id.textViewBookingDateTime)
        val textViewServicesBooked = itemView?.findViewById<TextView>(R.id.textViewServicesBooked)
        val imageview = itemView?.findViewById<ImageView>(R.id.imageviewCategory)
        val status = itemView?.findViewById<TextView>(R.id.status)

        textViewBookingDateTime?.text = "Date & Time: ${currentBooking?.bookingDateTime}"
        textViewServicesBooked?.text = "${currentBooking?.servicesBooked?.joinToString()}"
        currentBooking?.imageUrl?.let { imageUrl ->
            Picasso.get().load(imageUrl).into(imageview)
        }
        if (currentBooking?.status == "paymentdone") {
            // If true, set the text to "Status: Service Done"
            status?.text = "Status: Service Done"
        } else {
            status?.text = "Status: ${currentBooking?.status}"
        }

        // Set background color based on position
        val bgColor = if (position % 2 == 0) R.color.white else R.color.grey0
        itemView?.setBackgroundColor(ContextCompat.getColor(context, bgColor))

        return itemView!!
    }


}

private fun parseDate(dateString: String): Date {
    val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    return format.parse(dateString) ?: Date()
}
