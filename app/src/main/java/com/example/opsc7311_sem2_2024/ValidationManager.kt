package com.example.opsc7311_sem2_2024

import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
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
    fun validateConfirmPassword( passwordInput: TextInputLayout,  confirmPasswordInput: TextInputLayout ): Boolean {
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

    //Check if input is not null
    fun isTextNotEmpty(input: TextInputEditText, inputLayout: TextInputLayout): Boolean {
        val text = input.text.toString().trim()
        return if (text.isEmpty()) {
            inputLayout.error = "Field cannot be empty"
            false
        } else {
            inputLayout.error = null  // Clear the error
            true
        }
    }

    // Validate Min Hours considering Max Hours
    fun validateMinHours(input: TextInputEditText, inputLayout: TextInputLayout, maxInput: TextInputEditText): Boolean {
        val hoursText = input.text.toString().trim()
        val maxHoursText = maxInput.text.toString().trim()

        return if (hoursText.isEmpty()) {
            inputLayout.error = "Field cannot be empty"
            false
        } else {
            val minHours = hoursText.toInt()
            val maxHours = if (maxHoursText.isNotEmpty()) maxHoursText.toInt() else Int.MAX_VALUE // Default large value if maxHours is empty

            when {
                minHours < 1 -> {
                    inputLayout.error = "More"
                    false
                }
                minHours > 8 -> {
                    inputLayout.error = "Less"
                    false
                }
                minHours >= maxHours -> {
                    inputLayout.error = "Increase Max"
                    false
                }
                else -> {
                    inputLayout.error = null  // Clear the error
                    true
                }
            }
        }
    }

    // Validate Max Hours considering Min Hours
    fun validateMaxHours(input: TextInputEditText, inputLayout: TextInputLayout, minInput: TextInputEditText): Boolean {
        val hoursText = input.text.toString().trim()
        val minHoursText = minInput.text.toString().trim()

        return if (hoursText.isEmpty()) {
            inputLayout.error = "Field cannot be empty"
            false
        } else {
            val maxHours = hoursText.toInt()
            val minHours = if (minHoursText.isNotEmpty()) minHoursText.toInt() else Int.MIN_VALUE // Default small value if minHours is empty

            when {
                maxHours < 1 -> {
                    inputLayout.error = "More"
                    false
                }
                maxHours > 8 -> {
                    inputLayout.error = "Less"
                    false
                }
                maxHours <= minHours -> {
                    inputLayout.error = "Decrease Min"
                    false
                }
                else -> {
                    inputLayout.error = null  // Clear the error
                    true
                }
            }
        }
    }

    // Validate Task Time
    fun validateTaskTime(taskTimeInput: TextInputEditText, taskTimeLayout: TextInputLayout): Boolean {
        val taskTime = taskTimeInput.text.toString()

        // Check if the input is empty
        if (taskTime.isEmpty()) {
            taskTimeLayout.error = "Please enter task time"
            return false
        }

        try {
            // Split the input into hours and minutes
            val (hours, minutes) = taskTime.split(":").map { it.toInt() }

            // Validate the hours and minutes
            when {
                hours > 9 || (hours == 9 && minutes > 0) -> {
                    taskTimeLayout.error = "Task time cannot exceed 9 hours"
                    return false
                }
                hours == 0 && minutes < 30 -> {
                    taskTimeLayout.error = "Task time must be at least 30 minutes"
                    return false
                }
                else -> {
                    taskTimeLayout.error = null // Clear error if valid
                    return true
                }
            }
        } catch (e: Exception) {
            taskTimeLayout.error = "Invalid time format"
            return false
        }
    }

    //Check if ChipGroup have Chips
    fun isChipGroupNotEmpty(chipGroup: ChipGroup, inputLayout: TextInputLayout): Boolean {
        return if (chipGroup.childCount == 0) {
            inputLayout.error = "Please select at least one category"
            false
        } else {
            inputLayout.error = null  // Clear the error if there is at least one chip
            true
        }
    }
}
