package com.example.kinotheque.ui

import android.app.Application
import com.example.kinotheque.data.AppDatabase
import com.example.kinotheque.data.MovieRepository

class KinothequeApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MovieRepository(database.movieDao()) }
}
