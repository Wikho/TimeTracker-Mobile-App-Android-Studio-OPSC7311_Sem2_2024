package com.example.opsc7311_sem2_2024.LogSignIn

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.MainScreen
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.ValidationManager
import com.example.opsc7311_sem2_2024.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {
    // <editor-fold desc="Binding">
    private lateinit var binding: ActivityLogInBinding
    // </editor-fold>

    // <editor-fold desc="Firebase and Validation">
        private val validationManager = ValidationManager()
        private val firebaseManager = FirebaseManager()
        private lateinit var sharedPreferences: SharedPreferences
    // </editor-fold>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // <editor-fold desc="Initialize Shared Preferences && Check if the user is already logged in">
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Pre-fill email if "Remember Me" was checked
        val rememberMe = sharedPreferences.getBoolean("remember_me", false)
        if (rememberMe) {
            val savedEmail = sharedPreferences.getString("user_email", "")
            binding.etEmail.setText(savedEmail)
            binding.cbRemeberMe.isChecked = true
        }

        // Check if the user is already logged in
        checkLoginStatus()
        // </editor-fold>

        //Receive email from SignInActivity
        val emailFromSignUp = intent.getStringExtra("email")
        if (!emailFromSignUp.isNullOrEmpty()) {
            binding.etEmail.setText(emailFromSignUp)
        }


        // <editor-fold desc="Forgot Password Click Listener">
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
        // </editor-fold>

        // <editor-fold desc="Register Click Listener">
        binding.tvRegister.setOnClickListener {
            // Navigate to the SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        // </editor-fold>

        // <editor-fold desc="Login Button Click Listener">
        binding.btnLogIn.setOnClickListener {

            if (validateInputs()) {
                binding.btnLogIn.text = getString(R.string.loading)
                binding.btnLogIn.isEnabled = false
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val rememberMe = binding.cbRemeberMe.isChecked

                firebaseManager.loginUser(email, password) { success, message ->
                    binding.btnLogIn.text = getString(R.string.log_in)
                    binding.btnLogIn.isEnabled = true
                    if (success) {
                        fetchUserInfoAndProceed(rememberMe)
                    } else {
                        Toast.makeText(this, "Password or Email incorrect.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        // </editor-fold>
    }

    // <editor-fold desc="Validate Inputs">
    private fun validateInputs(): Boolean {
        val isEmailValid = validationManager.validateEmail(binding.tilEmail)
        val isPasswordValid = binding.etPassword.text.toString().isNotEmpty()

        if (!isPasswordValid) {
            binding.tilPassword.error = "Password is required"
        } else {
            binding.tilPassword.error = null
        }

        return isEmailValid && isPasswordValid
    }
    // </editor-fold>

    // <editor-fold desc="Check Login Status">
    private fun checkLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // User is signed in and email is verified
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            finish()
        }
    }
    // </editor-fold>

    // <editor-fold desc="Save Login State">
    private fun saveLoginState(rememberMe: Boolean, userEmail: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", rememberMe)
        if (rememberMe) {
            editor.putString("user_email", userEmail)
        } else {
            editor.remove("user_email")
        }
        editor.apply()
    }
    // </editor-fold>

    // <editor-fold desc="Fetch Info">
    private fun fetchUserInfoAndProceed(rememberMe: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        databaseRef.get().addOnSuccessListener { dataSnapshot ->
            val userName = dataSnapshot.child("name").getValue(String::class.java) ?: "User Name"
            val userEmail = dataSnapshot.child("email").getValue(String::class.java) ?: "user@example.com"

            saveLoginState(rememberMe, userEmail)

            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to retrieve user info.", Toast.LENGTH_SHORT).show()
        }
    }
    // </editor-fold>

}
