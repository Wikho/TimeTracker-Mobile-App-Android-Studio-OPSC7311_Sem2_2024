package com.example.opsc7311_sem2_2024

import com.google.firebase.database.FirebaseDatabase

class FirebaseTaskManager {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

    // Save task to Firebase under the user's folder
    fun addTaskToFirebase(userId: String, taskItem: TaskItem) {
        val taskReference = databaseReference.child(userId).child("Task").child(taskItem.id)
        taskReference.setValue(taskItem)
            .addOnSuccessListener { /* Task saved successfully */ }
            .addOnFailureListener { /* Handle failure */ }
    }

    // Retrieve a task from Firebase using its ID
    fun getTaskFromFirebase(userId: String, taskId: String, callback: (TaskItem?) -> Unit) {
        val taskReference = databaseReference.child(userId).child("Task").child(taskId)
        taskReference.get().addOnSuccessListener {
            val taskItem = it.getValue(TaskItem::class.java)
            callback(taskItem)
        }.addOnFailureListener {
            callback(null)
        }
    }

    // Delete a task from Firebase
    fun deleteTaskFromFirebase(userId: String, taskId: String) {
        val taskReference = databaseReference.child(userId).child("Task").child(taskId)
        taskReference.removeValue()
            .addOnSuccessListener { /* Task deleted successfully */ }
            .addOnFailureListener { /* Handle failure */ }
    }
}