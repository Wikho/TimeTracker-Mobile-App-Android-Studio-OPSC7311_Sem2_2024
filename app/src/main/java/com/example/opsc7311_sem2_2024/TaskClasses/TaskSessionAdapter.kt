package com.example.opsc7311_sem2_2024.TaskClasses

// TaskSessionAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.databinding.SessionItemLayoutBinding // Update the package path



class TaskSessionAdapter : RecyclerView.Adapter<TaskSessionAdapter.SessionViewHolder>()  {

    private val sessionList = mutableListOf<TaskSession>()

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
        holder.binding.tvStartTime.text = "Started: ${session.startTime}"
        holder.binding.tvEndTime.text = "Ended: ${session.endTime}"
        holder.binding.tvSessionDuration.text = "Duration: ${session.sessionDuration}"
        holder.binding.tvSessionDescription.text = "Description: ${session.sessionDescription}"

        // Load image using Glide
        if (!session.imagePath.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(session.imagePath)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.binding.ivSessionImage)
        } else {
            holder.binding.ivSessionImage.setImageResource(R.drawable.ic_launcher_background)
        }

    }

    override fun getItemCount() = sessionList.size

    // Function to load an image into the ImageView (pseudo code)
    private fun loadImageIntoView(imageView: ImageView, imagePath: String) {
        // Use your preferred image loading library, like Glide or Picasso
    }

    fun submitList(sessions: List<TaskSession>) {
        sessionList.clear()
        sessionList.addAll(sessions)
        notifyDataSetChanged()
    }
}
