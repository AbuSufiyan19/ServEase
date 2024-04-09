package com.project.mad

import UserProfileFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserHomePageActivity : AppCompatActivity() {

    private var lastClickTime: Long = 0
    private val cooldownDuration: Long = 1000 // 2 seconds cooldown

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home_page)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= cooldownDuration) {
                // Update last click time
                lastClickTime = currentTime

                when (menuItem.itemId) {
                    R.id.navigation_home -> {
                        replaceFragment(UserHomeFragment())
                        true
                    }
                    R.id.navigation_account ->{
                        replaceFragment(UserProfileFragment())
                        true
                    }
                    R.id.navigation_booking ->{
                        replaceFragment(UserBookingFragment())
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
        replaceFragment(UserHomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
