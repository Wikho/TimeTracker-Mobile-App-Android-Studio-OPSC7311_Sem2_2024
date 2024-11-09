package com.example.opsc7311_sem2_2024

import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.*

class FirebaseManager{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // <editor-fold desc="Get User Info">

    // Function to get the user's name
    fun getUserName(onResult: (String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("Users").child(userId).child("User Info").child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.getValue(String::class.java)
                        onResult(name)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onResult(null)
                    }
                })
        } else {
            onResult(null)
        }
    }

    // Function to get the user's email
    fun getUserEmail(onResult: (String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("Users").child(userId).child("User Info").child("email")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val email = snapshot.getValue(String::class.java)
                        onResult(email)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onResult(null)
                    }
                })
        } else {
            onResult(null)
        }
    }


    // </editor-fold>

    // <editor-fold desc="Login/Sign in Functions">

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
                            // Store user details in Realtime Database under "User Info"
                            val userId = user.uid
                            val userData = mapOf(
                                "name" to name,
                                "email" to email
                            )
                            database.getReference("Users").child(userId).child("User Info")
                                .setValue(userData)
                                .addOnCompleteListener { dbTask ->
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

    // </editor-fold>

    // <editor-fold desc="Task Functions">

    // Save Task under the current user
    fun saveTask(task: TaskItem, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val taskRef = database.getReference("Users").child(userId).child("tasks").child(task.id)

        taskRef.setValue(task).addOnCompleteListener { taskResult ->
            if (taskResult.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, taskResult.exception?.message)
            }
        }
    }

    // Fetch Tasks for the current user
    fun fetchTasks(onComplete: (List<TaskItem>) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val tasksRef = database.getReference("Users").child(userId).child("tasks")

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<TaskItem>()
                snapshot.children.forEach { taskSnapshot ->
                    val task = taskSnapshot.getValue(TaskItem::class.java)
                    task?.let { tasks.add(it) }
                }
                onComplete(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(emptyList())
            }
        })
    }

    // Update Task
    fun updateTask(task: TaskItem, onComplete: (Boolean, String?) -> Unit) {
        saveTask(task, onComplete)
    }

    // Delete Task
    fun deleteTask(taskId: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val taskRef = database.getReference("Users").child(userId).child("tasks").child(taskId)

        taskRef.removeValue().addOnCompleteListener { taskResult ->
            if (taskResult.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, taskResult.exception?.message)
            }
        }
    }

    // </editor-fold>

    // <editor-fold desc="Categories Functions">

    // Save Categories under the current user
    fun saveCategories(categories: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val categoriesRef = database.getReference("Users").child(userId).child("Categories")
        categoriesRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val existingCategories = currentData.value as? Map<String, Any?> ?: emptyMap()
                val updatedCategories = existingCategories.toMutableMap()
                for (category in categories) {
                    updatedCategories[category.uppercase()] = true
                }
                currentData.value = updatedCategories
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Handle completion if necessary
            }
        })
    }

    // Fetch Categories for the current user
    fun fetchCategories(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val categoriesRef = database.getReference("Users").child(userId).child("Categories")
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (childSnapshot in snapshot.children) {
                    val category = childSnapshot.key?.uppercase()
                    category?.let {
                        categories.add(it)
                    }
                }
                onResult(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    // Function to fetch the categories for a specific Task ID
    fun fetchCategoriesByTaskId(taskId: String, onResult: (List<String>) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val taskRef = database.getReference("Users").child(userId).child("tasks").child(taskId)

        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(TaskItem::class.java)
                val categories = task?.category?.split(",")?.map { it.trim().uppercase() } ?: listOf("UNDEFINED")
                onResult(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(listOf("UNDEFINED"))
            }
        })
    }


    // </editor-fold>
}
