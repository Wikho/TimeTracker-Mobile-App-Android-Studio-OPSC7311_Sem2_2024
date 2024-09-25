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

        // <editor-fold desc="Reset Password Button ">
        binding.btnReset.setOnClickListener {
            if (validateEmail()) {
                val email = binding.etEmail.text.toString().trim()
                firebaseManager.resetPassword(email) { success, message ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "Password reset email sent. Check your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Optional: Close ForgotPassword after sending the reset link
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // </editor-fold>

        // <editor-fold desc="Reset Password Button ">

        binding.fabtnBack.setOnClickListener {
            finish()
        }




        // </editor-fold>
    }

    // <editor-fold desc="Validate Email">
    private fun validateEmail(): Boolean {
        return validationManager.validateEmail(binding.tilEmail)
    }
    // </editor-fold>
}
