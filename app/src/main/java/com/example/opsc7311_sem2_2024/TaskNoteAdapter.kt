package com.example.opsc7311_sem2_2024

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.ItemTaskNoteBinding

class TaskNoteAdapter(private val listener: NoteActionListener) :
    RecyclerView.Adapter<TaskNoteAdapter.TaskNoteViewHolder>() {

    private val notesList = mutableListOf<TaskNote>()

    fun submitList(notes: List<TaskNote>) {
        notesList.clear()
        notesList.addAll(notes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskNoteViewHolder {
        val binding = ItemTaskNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskNoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskNoteViewHolder, position: Int) {
        val note = notesList[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int = notesList.size

    inner class TaskNoteViewHolder(private val binding: ItemTaskNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: TaskNote) {
            binding.cbNoteCompleted.isChecked = note.isCompleted
            binding.tvNoteContent.text = note.content

            // Strikethrough text if completed
            if (note.isCompleted) {
                binding.tvNoteContent.paintFlags = binding.tvNoteContent.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvNoteContent.paintFlags = binding.tvNoteContent.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Checkbox change listener
            binding.cbNoteCompleted.setOnCheckedChangeListener { _, isChecked ->
                listener.onNoteChecked(note, isChecked)
                // Update strikethrough
                if (isChecked) {
                    binding.tvNoteContent.paintFlags = binding.tvNoteContent.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    binding.tvNoteContent.paintFlags = binding.tvNoteContent.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

            // Delete button listener
            binding.btnDeleteNote.setOnClickListener {
                listener.onNoteDeleted(note)
            }
        }
    }

    interface NoteActionListener {
        fun onNoteChecked(note: TaskNote, isChecked: Boolean)
        fun onNoteDeleted(note: TaskNote)
    }
}
