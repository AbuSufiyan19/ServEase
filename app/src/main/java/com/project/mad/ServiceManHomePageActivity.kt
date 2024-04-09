package com.project.mad

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ServiceManHomePageActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private var lastClickTime: Long = 0
    private val cooldownDuration: Long = 1500 // 2 seconds cooldown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_man_home_page)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= cooldownDuration) {
                // Update last click time
                lastClickTime = currentTime
            when (menuItem.itemId) {
                R.id.navigation_updateservice -> {
                    replaceFragment(ServiceUpdateFragment())
                    true
                }
                R.id.navigation_account -> {
                    replaceFragment(ServiceProfileFragment())
                    true
                }
                R.id.navigation_home -> {
                    replaceFragment(SP_Service_Home_Fragment())
                    true
                }
                R.id.navigation_bookings -> {
                    replaceFragment(SP_Service_Bookings_Fragment())
                    true
                }

                // Add more cases for other menu items if needed
                else -> false
            }
            } else {
                // Click is within cooldown duration, do nothing
                true
            }
        }
        // Initially display UserHomeFragment
        replaceFragment(SP_Service_Home_Fragment())
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
