package com.example.opsc7311_sem2_2024

import com.google.android.material.textfield.TextInputLayout

class ValidationManager {

    // Validate Name
    fun validateName(nameInput: TextInputLayout): Boolean {
        val name = nameInput.editText?.text.toString().trim()
        val namePattern = "^[a-zA-Z]+(?: [a-zA-Z]+)*$" // Allows only letters and spaces between words

        return if (name.isEmpty() || !name.matches(namePattern.toRegex())) {
            nameInput.error = "Name must contain only letters and no spaces"
            false
        } else {
            nameInput.error = null
            true
        }
    }

    // Validate Email
    fun validateEmail(emailInput: TextInputLayout): Boolean {
        val email = emailInput.editText?.text.toString().trim()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (email.isEmpty() || !email.matches(emailPattern.toRegex())) {
            emailInput.error = "Valid email is required"
            false
        } else {
            emailInput.error = null
            true
        }
    }

    // Validate Password
    fun validatePassword(passwordInput: TextInputLayout): Boolean {
        val password = passwordInput.editText?.text.toString().trim()
        return if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            false
        } else {
            passwordInput.error = null
            true
        }
    }

    // Validate Confirm Password: should match the password
    fun validateConfirmPassword(
        passwordInput: TextInputLayout,
        confirmPasswordInput: TextInputLayout
    ): Boolean {
        val password = passwordInput.editText?.text.toString().trim()
        val confirmPassword = confirmPasswordInput.editText?.text.toString().trim()
        return if (confirmPassword != password) {
            confirmPasswordInput.error = "Passwords do not match"
            false
        } else {
            confirmPasswordInput.error = null
            true
        }
    }
}
