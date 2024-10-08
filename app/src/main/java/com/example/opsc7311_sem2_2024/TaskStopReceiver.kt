package com.example.opsc7311_sem2_2024

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("taskId")
        // Handle stopping the task
        if (context is MainScreen) {
            val fragment = context.supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is TasksFragment) {
                fragment.stopTaskFromNotification(taskId)
            }
        }
    }

}