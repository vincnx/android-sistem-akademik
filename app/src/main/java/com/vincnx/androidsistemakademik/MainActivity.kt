package com.vincnx.androidsistemakademik

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vincnx.androidsistemakademik.R
import androidx.navigation.NavController
import android.view.View
import com.vincnx.androidsistemakademik.data.source.local.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize SessionManager
        sessionManager = (application as MyApplication).appContainer.sessionManager
        
        // Get the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Get the NavController
        val navController = navHostFragment.navController
        
        // Set up Bottom Navigation
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        
        // Set up navigation visibility
        setupNavigationVisibility(navController)
        
        // Update menu items based on user role
        updateMenuItemsVisibility()
    }

    private fun setupNavigationVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.loginFragment) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
                // Update menu items visibility when navigation changes (except for login)
                updateMenuItemsVisibility()
            }
        }
    }

    private fun updateMenuItemsVisibility() {
        val menu = bottomNav.menu
        val userRole = sessionManager.getUserDetails()[SessionManager.KEY_ROLE]

        // Hide all role-specific items first
        menu.findItem(R.id.courseFragment)?.isVisible = false
        menu.findItem(R.id.lecturerEnrollFragment)?.isVisible = false
        menu.findItem(R.id.coordinatorCourseFragment)?.isVisible = false

        // Show items based on role
        when (userRole) {
            "student" -> {
                menu.findItem(R.id.courseFragment)?.isVisible = true
            }
            "lecturer" -> {
                menu.findItem(R.id.lecturerEnrollFragment)?.isVisible = true
            }
            "coordinator" -> {
                menu.findItem(R.id.coordinatorCourseFragment)?.isVisible = true
            }
        }

        // Profile is visible for all authenticated users
        menu.findItem(R.id.navigation_profile)?.isVisible = sessionManager.isLoggedIn()
    }
}