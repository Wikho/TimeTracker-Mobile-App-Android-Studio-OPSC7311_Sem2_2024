package com.example.opsc7311_sem2_2024

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.ItemNoteBinding

class NotesAdapter(
    private val notes: MutableList<Note>,
    private val listener: NoteItemListener
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    interface NoteItemListener {
        fun onNoteChecked(note: Note, isChecked: Boolean)
        fun onNoteDeleted(note: Note)
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.cbNoteCompleted.isChecked = note.isCompleted
            binding.tvNoteContent.text = note.content

            // Apply strikethrough if completed
            binding.tvNoteContent.paintFlags = if (note.isCompleted) {
                binding.tvNoteContent.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvNoteContent.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Checkbox change listener
            binding.cbNoteCompleted.setOnCheckedChangeListener { _, isChecked ->
                note.isCompleted = isChecked
                listener.onNoteChecked(note, isChecked)
                // Update the text appearance
                //notifyItemChanged(adapterPosition)
            }

            // Delete button listener
            binding.btnDeleteNote.setOnClickListener {
                listener.onNoteDeleted(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    fun addNote(note: Note) {
        notes.add(note)
        notifyItemInserted(notes.size - 1)
    }

    fun removeNote(note: Note) {
        val index = notes.indexOf(note)
        if (index != -1) {
            notes.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
