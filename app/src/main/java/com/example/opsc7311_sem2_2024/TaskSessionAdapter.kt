package com.example.opsc7311_sem2_2024

// TaskSessionAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.SessionItemLayoutBinding // Update the package path

class TaskSessionAdapter(private val sessionList: List<TaskSession>) :
    RecyclerView.Adapter<TaskSessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(val binding: SessionItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        // Inflate the layout using View Binding
        val binding = SessionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessionList[position]

        // Bind data using the binding object
        holder.binding.tvSessionStartDate.text = "Date: ${session.sessionStartDate}"
        holder.binding.tvStartTime.text = "Time Started: ${session.startTime}"
        holder.binding.tvEndTime.text = "Time Ended: ${session.endTime}"
        holder.binding.tvSessionDuration.text = "Session Duration: ${session.sessionDuration}"

        // Load the image (pseudo code for loading the image)
        loadImageIntoView(holder.binding.ivSessionImage, session.imagePath)
    }

    override fun getItemCount() = sessionList.size

    // Function to load an image into the ImageView (pseudo code)
    private fun loadImageIntoView(imageView: ImageView, imagePath: String) {
        // Use your preferred image loading library, like Glide or Picasso
    }
}
