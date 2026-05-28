package com.kinoteka.app

import android.app.Application
import com.kinoteka.app.data.local.MovieDatabase
import com.kinoteka.app.data.repository.MovieRepository

class KinotekaApplication : Application() {
    
    val database: MovieDatabase by lazy {
        MovieDatabase.getDatabase(this)
    }
    
    val repository: MovieRepository by lazy {
        MovieRepository(database.movieDao(), this)
    }
}
