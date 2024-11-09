
package com.example.opsc7311_sem2_2024.Notes

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.databinding.ItemNoteBinding

class NotesAdapter(
    private val notes: MutableList<Note>,
    private val listener: NoteItemListener
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    // <editor-fold desc="NoteItemListener Interface">
    interface NoteItemListener {
        fun onNoteChecked(note: Note)
        fun onNoteLongPressed(note: Note)
    }
    // </editor-fold>

    // <editor-fold desc="NoteViewHolder Class">
    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.cbNoteCompleted.isChecked = note.isCompleted
            binding.tvNoteTitle.text = note.title

            // Description visibility
            if (note.description.isNotEmpty()) {
                binding.tvNoteDescription.text = note.description
                binding.tvNoteDescription.visibility = View.VISIBLE
            } else {
                binding.tvNoteDescription.visibility = View.GONE
            }

            // Apply strikethrough if completed
            if (note.isCompleted) {
                binding.tvNoteTitle.paintFlags =
                    binding.tvNoteTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvNoteTitle.paintFlags =
                    binding.tvNoteTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Checkbox change listener
            binding.cbNoteCompleted.setOnCheckedChangeListener { _, isChecked ->
                note.isCompleted = isChecked
                listener.onNoteChecked(note)
            }

            // Long press to show options
            binding.root.setOnLongClickListener {
                listener.onNoteLongPressed(note)
                true
            }

            // Set background color based on importance
            val importanceColor = when (note.importance) {
                "High" -> ContextCompat.getColor(binding.root.context, R.color.highImportance)
                "Medium" -> ContextCompat.getColor(binding.root.context, R.color.mediumImportance)
                "Low" -> ContextCompat.getColor(binding.root.context, R.color.lowImportance)
                else -> ContextCompat.getColor(binding.root.context, R.color.white)
            }
            binding.root.setBackgroundColor(importanceColor)
        }
    }
    // </editor-fold>

    // <editor-fold desc="RecyclerView.Adapter Overrides">
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }
    // </editor-fold>
}
