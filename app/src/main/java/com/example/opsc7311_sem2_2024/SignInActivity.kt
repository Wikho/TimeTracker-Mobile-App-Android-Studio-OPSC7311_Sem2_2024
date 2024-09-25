package com.example.opsc7311_sem2_2024

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opsc7311_sem2_2024.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    // <editor-fold desc="View Binding">
    private lateinit var binding: ActivitySignInBinding
    // </editor-fold>

    // <editor-fold desc="FireBase">
    private val validationManager = ValidationManager()
    private val firebaseManager = FirebaseManager()
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // <editor-fold desc="Binding Setup">
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // </editor-fold>

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // <editor-fold desc="Btn click">

        //Btn go to LogIn Page
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //Btn go to SignIn Page
        binding.btnSignUp.setOnClickListener {
            if (validateInputs()) {
                // Set button text to "Loading"
                binding.btnSignUp.text = getString(R.string.loading)
                // Disable the button to prevent multiple clicks
                binding.btnSignUp.isEnabled = false

                val name = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                firebaseManager.registerUser(name, email, password) { success, message ->
                    // Restore button text and state when task is complete
                    binding.btnSignUp.text = getString(R.string.sign_up)
                    binding.btnSignUp.isEnabled = true

                    if (success) {
                        Toast.makeText(this, "Registration successful! Please verify your email address.", Toast.LENGTH_LONG).show()

                        // Navigate to LoginActivity and pass the email
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("email", email) // Pass the email to LoginActivity
                        startActivity(intent)
                        finish()

                    } else {
                        if (message == "email_exists") {
                            Toast.makeText(
                                this,
                                "Email already used, Log in.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        // </editor-fold>

    }

    // <editor-fold desc="Validate Inputs using ValidationManager">

    private fun validateInputs(): Boolean {
        val isNameValid = validationManager.validateName(binding.tilName)
        val isEmailValid = validationManager.validateEmail(binding.tilEmail)
        val isPasswordValid = validationManager.validatePassword(binding.tilPassword)
        val isConfirmPasswordValid = validationManager.validateConfirmPassword(
            binding.tilPassword,
            binding.tilPasswordConfirm
        )

        return isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }
    // </editor-fold>
}