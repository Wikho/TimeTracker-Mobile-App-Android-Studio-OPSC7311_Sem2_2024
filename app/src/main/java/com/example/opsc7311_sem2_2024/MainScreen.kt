package com.example.opsc7311_sem2_2024

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.opsc7311_sem2_2024.Analytics.AnalyticsFragment
import com.example.opsc7311_sem2_2024.Calendar.CalendarFragment
import com.example.opsc7311_sem2_2024.LogSignIn.LoginActivity
import com.example.opsc7311_sem2_2024.Notes.NotesFragment
import com.example.opsc7311_sem2_2024.Pomodoro.PomodoroFragment
import com.example.opsc7311_sem2_2024.Settings.SettingsFragment
import com.example.opsc7311_sem2_2024.Tasks.ArchivedTasksFragment
import com.example.opsc7311_sem2_2024.Tasks.TasksFragment
import com.example.opsc7311_sem2_2024.databinding.ActivityMainScreenBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityMainScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    private val firebaseManager = FirebaseManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        // Populate nav_header with user info
        populateNavHeader()

        // </editor-fold>

    }

    // <editor-fold desc="Burger Menu Functions">

    private fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)                          //Sir i used R.id instead of binding because it the correct way of making it procedural.
        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nvTask -> replaceFragment(TasksFragment())
            R.id.nvCalendar -> replaceFragment(CalendarFragment())
            R.id.nvAnalytics -> replaceFragment(AnalyticsFragment())
            R.id.nvArchivedTasks -> replaceFragment(ArchivedTasksFragment())
            R.id.nvNotes -> replaceFragment(NotesFragment())
            R.id.nvPomodoro -> replaceFragment(PomodoroFragment())
            R.id.nvSettings -> replaceFragment(SettingsFragment())
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
        editor.clear()
        editor.apply()

        // Sign out from FirebaseAuth
        FirebaseAuth.getInstance().signOut()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun populateNavHeader() {
        val navigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvNavHeaderUserName)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvNavHeaderUserEmail)

        // Fetch and set the user's name
        firebaseManager.getUserName { userName ->
            tvUserName.text = userName ?: getString(R.string.user_name)
        }

        // Fetch and set the user's email
        firebaseManager.getUserEmail { userEmail ->
            tvUserEmail.text = userEmail ?: getString(R.string.user_email)
        }
    }


}

