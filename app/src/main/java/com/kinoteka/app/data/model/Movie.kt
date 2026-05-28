package com.kinoteka.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val year: Int,
    val description: String,
    val posterPath: String?,
    val genre: String,
    val rating: Int,
    val createdAt: Long = System.currentTimeMillis()
)
