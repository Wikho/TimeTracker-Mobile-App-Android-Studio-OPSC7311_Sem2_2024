package com.example.opsc7311_sem2_2024

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromTaskNoteList(value: MutableList<TaskNote>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTaskNoteList(value: String): MutableList<TaskNote> {
        val gson = Gson()
        val listType = object : TypeToken<MutableList<TaskNote>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Existing converters for sessionHistory if any...
}
