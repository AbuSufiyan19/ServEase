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

class BookingAdapter(context: Context, bookingsList: List<UserBookingFragment.Booking>) :
    ArrayAdapter<UserBookingFragment.Booking>(context, 0, bookingsList.reversed()) {

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

        textViewBookingDateTime?.text = "Date & Time: ${currentBooking?.bookingDateTime}"
        textViewServicesBooked?.text = "${currentBooking?.servicesBooked?.joinToString()}"
        currentBooking?.imageUrl?.let { imageUrl ->
            Picasso.get().load(imageUrl).into(imageview)
        }

        // Set background color based on position
        val bgColor = if (position % 2 == 0) R.color.white else R.color.grey0
        itemView?.setBackgroundColor(ContextCompat.getColor(context, bgColor))

        return itemView!!
    }
}
