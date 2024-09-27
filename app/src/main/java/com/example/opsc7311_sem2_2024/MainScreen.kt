package com.example.opsc7311_sem2_2024

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.opsc7311_sem2_2024.databinding.ActivityMainScreenBinding
import com.google.android.material.navigation.NavigationView

class MainScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityMainScreenBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //    insets
        //}

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


        // <editor-fold desc="Burger Menu">

        drawerLayout = binding.drawerLayout

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this,drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        if (savedInstanceState == null){
            replaceFragment(TasksFragment())
            navigationView.setCheckedItem(R.id.nvTask)
        }

        // </editor-fold>


    }

    // <editor-fold desc="Burger Menu Functions">

    private fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)                          //Sir i used R.id instead of binding because it the correct way of making it procedural.
        transaction.commit()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                // Execute the default back action
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nvTask -> replaceFragment(TasksFragment())
            R.id.nvCalendar -> replaceFragment(CalendarFragment())
            R.id.nvAnalytics -> replaceFragment(AnalyticsFragment())
            R.id.nvArchivedTasks -> replaceFragment(ArchivedTasksFragment())
            R.id.nvNotes -> replaceFragment(NotesFragment())
            R.id.nvSettings -> replaceFragment(SettingsFragment())
            R.id.nvPomodoro -> replaceFragment(PomodoroFragment())
            R.id.nvLogout -> logOutUser()
        }

        //Close the drawer after a selection is made
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // </editor-fold>

    private fun logOutUser() {
        // Clear login state
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.apply()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}