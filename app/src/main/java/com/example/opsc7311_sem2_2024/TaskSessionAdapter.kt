package com.example.opsc7311_sem2_2024

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskSessionAdapter(private val sessionList: List<TaskSession>) :
    RecyclerView.Adapter<TaskSessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startTime: TextView = view.findViewById(R.id.tv_StartTime)
        val endTime: TextView = view.findViewById(R.id.tv_EndTime)
        val sessionDuration: TextView = view.findViewById(R.id.tv_SessionDuration)
        val sessionImage: ImageView = view.findViewById(R.id.iv_SessionImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_item_layout, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessionList[position]
        holder.startTime.text = "Time Started: ${session.startTime}"
        holder.endTime.text = "Time Ended: ${session.endTime}"
        holder.sessionDuration.text = "Session Duration: ${session.sessionDuration}"

        // Load the image from the path, assuming imagePath is a valid path or URI
        loadImageIntoView(holder.sessionImage, session.imagePath)
    }

    override fun getItemCount() = sessionList.size

    // Function to load an image into the ImageView (pseudo code)
    private fun loadImageIntoView(imageView: ImageView, imagePath: String) {
        // Use your preferred image loading library, like Glide or Picasso
    }
}