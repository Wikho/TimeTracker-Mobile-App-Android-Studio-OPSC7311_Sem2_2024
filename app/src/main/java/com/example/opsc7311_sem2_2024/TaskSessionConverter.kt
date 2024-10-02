package com.example.opsc7311_sem2_2024

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskSessionConverter {

    @TypeConverter
    fun fromTaskSessionList(value: MutableList<TaskSession>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toTaskSessionList(value: String): MutableList<TaskSession> {
        val listType = object : TypeToken<MutableList<TaskSession>>() {}.type
        return Gson().fromJson(value, listType)
    }

}