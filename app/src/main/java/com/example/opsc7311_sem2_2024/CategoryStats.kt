package com.example.opsc7311_sem2_2024

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_stats")

data class CategoryStats(
    @PrimaryKey val categoryName: String,
    var totalMinutes: Int = 0
)