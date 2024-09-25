package com.example.opsc7311_sem2_2024

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.databinding.ActivityLogInBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private val validationManager = ValidationManager()
    private val firebaseManager = FirebaseManager()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // <editor-fold desc="Initialize Shared Preferences && Check if the user is already logged in">
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Check if the user is already logged in
        checkLoginStatus()
        // </editor-fold>

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
                        saveLoginState(rememberMe) // Save login state based on Remember Me
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
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
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            // Redirect to MainActivity if already logged in
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    // </editor-fold>

    // <editor-fold desc="Save Login State">
    private fun saveLoginState(rememberMe: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", rememberMe)
        editor.apply()
    }
    // </editor-fold>
}
