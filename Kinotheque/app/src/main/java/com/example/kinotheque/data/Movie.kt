package com.example.kinotheque.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val year: Int,
    val description: String,
    val genre: String,
    val rating: Int,
    val posterPath: String? = null
)
