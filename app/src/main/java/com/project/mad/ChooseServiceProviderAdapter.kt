import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.project.mad.ChooseServiceProvider
import com.project.mad.R

class ChooseServiceProviderAdapter(private val context: Context, private val serviceMenList: List<ChooseServiceProvider.ServiceMan>) : BaseAdapter() {

    // Current selected position
    private var selectedPosition = -1

    override fun getCount(): Int {
        return serviceMenList.size
    }

    override fun getItem(position: Int): Any {
        return serviceMenList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemView = convertView
        val viewHolder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.choose_list_item, parent, false)
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        val serviceMan = serviceMenList[position]

        // Bind data to views
        viewHolder.usernameTextView.text = serviceMan.username

        // Check if rating is empty or null, assign "0" if it is
        val rating = if (serviceMan.rating.isNullOrBlank()) "0" else serviceMan.rating
        // Assuming viewHolder is the ViewHolder object where ratingTextView is defined
        if (rating == "0.0") {
            viewHolder.ratingTextView.text = "N/A"
        } else {
            viewHolder.ratingTextView.text = rating.toString()
        }


        // Set background color based on position
        val bgColor = if (position % 2 == 0) R.color.grey1 else R.color.grey
        itemView?.setBackgroundColor(ContextCompat.getColor(context, bgColor))


        // Set the radio button state
        viewHolder.radioButton.isChecked = position == selectedPosition

        // Handle radio button click
        viewHolder.radioButton.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }

        return itemView!!
    }

    // Get the selected service man
    fun getSelectedServiceMan(): ChooseServiceProvider.ServiceMan? {
        return if (selectedPosition != -1) {
            serviceMenList[selectedPosition]
        } else {
            null
        }
    }

    // ViewHolder pattern for efficient view recycling
    private class ViewHolder(view: View) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val ratingTextView: TextView = view.findViewById(R.id.ratingTextView)
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)
    }
}
