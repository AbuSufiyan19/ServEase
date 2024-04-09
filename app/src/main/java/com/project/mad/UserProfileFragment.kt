import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.project.mad.R
import com.google.firebase.database.*
import com.project.mad.LoginActivity
import com.project.mad.MainActivity
import java.io.IOException
import java.util.Locale


class UserProfileFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize TextViews
        nameTextView = view.findViewById(R.id.user_name)
        emailTextView = view.findViewById(R.id.user_email)
        phoneTextView = view.findViewById(R.id.user_phone)
        addressTextView = view.findViewById(R.id.user_address)


        // Fetch user data
        fetchUserData()

        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }
        return view
    }

    // Method to fetch user data
    private fun fetchUserData() {
        // Retrieve user token from SharedPreferences
        val sharedPreferences =
            requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userToken", "")


        val databaseReference = FirebaseDatabase.getInstance().reference
        // Query the users collection to check if the provided userId exists
        if (userId != null) {
            databaseReference.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // If the user exists, retrieve its data
                            val userData = dataSnapshot.getValue(User::class.java)

                            // Assuming User class has properties name, email, phone, and address
                            val name = userData?.username ?: ""
                            val email = userData?.email ?: ""
                            val phone = userData?.phoneNumber ?: ""
                            val latitude = userData?.location?.latitude ?: 0.0
                            val longitude = userData?.location?.longitude ?: 0.0
                            fetchAddressFromLocation(latitude, longitude)


                            // Assign user data to respective TextViews
                            nameTextView.text = name
                            emailTextView.text = email
                            phoneTextView.text = phone

                        } else {
                            // Handle the case where the provided userId doesn't exist
                            // For example, display a message indicating user not found
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
        }
    }

    data class User(
        val username: String = "",
        val email: String = "",
        val phoneNumber: String = "",
        val location: Location = Location(),
        val address: String = ""
    )

    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )

    private fun fetchAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    addressTextView.text = address
                } else {
                    // Handle case where no address is found
                    addressTextView.text = "Address not found"
                }
            }
        } catch (e: IOException) {
            // Handle IO exception
            addressTextView.text = "Error fetching address"
        }
    }

    private fun logout() {
        // Clear all user data from SharedPreferences
        val sharedPreferences =
            requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()


        // Navigate to the login activity
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // Finish the current activity to prevent returning to it by pressing back
    }
}
