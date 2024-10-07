package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.databinding.ActivityForgotPasswordBinding

class ForgotPassword : AppCompatActivity() {

    // <editor-fold desc="Binding">
    private lateinit var binding: ActivityForgotPasswordBinding
    // </editor-fold>

    // <editor-fold desc="Firebase ">
    private val validationManager = ValidationManager()
    private val firebaseManager = FirebaseManager()
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseManager
        val firebaseManager = FirebaseManager()

        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                firebaseManager.resetPassword(email) { success, message ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "Password reset email sent. Check your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Optional: Close ForgotPassword after sending the reset link
                    } else {
                        Toast.makeText(this, message ?: "Error sending reset email.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.tilEmail.error = "Email is required"
            }
        }

        //Back Button
        binding.fabtnBack.setOnClickListener {
            finish()
        }

    }
}
