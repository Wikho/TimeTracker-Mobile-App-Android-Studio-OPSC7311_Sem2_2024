package com.example.opsc7311_sem2_2024

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class FirebaseManager{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("Users")


    // Function to register user with email and password
    fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Send verification email
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            // Store user details in Realtime Database
                            val userId = user.uid
                            val userData = mapOf(
                                "name" to name,
                                "email" to email
                            )
                            database.child(userId).setValue(userData).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onResult(true, null) // Registration successful
                                } else {
                                    onResult(false, dbTask.exception?.message)
                                }
                            }
                        } else {
                            onResult(false, "Failed to send verification email. Please try again.")
                        }
                    }
                } else {
                    // Check if the error is due to an existing email
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        onResult(false, "email_exists")
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
            }
    }

    // Function to log in user, ensuring they have verified their email
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check if email is verified
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        onResult(true, null) // Login successful
                    } else {
                        // Sign out the user if not verified and prompt to check email
                        auth.signOut()
                        onResult(false, "Please verify your email address. A verification link has been sent to your email.")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Reset Password
    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Search if email exist
    @Suppress("DEPRECATION")
    fun doesEmailExist(email: String, callback: (Boolean) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (!signInMethods.isNullOrEmpty()) {
                        // Email exists
                        callback(true)
                    } else {
                        // Email does not exist
                        callback(false)
                    }
                } else {
                    // Task failed, assume email does not exist
                    callback(false)
                }
            }
    }

}
