package com.vincnx.androidsistemakademik.data.source.db

import com.google.firebase.database.FirebaseDatabase

object DatabaseClient {
    val database: FirebaseDatabase by lazy {
        val instance = FirebaseDatabase.getInstance()
        instance.setPersistenceEnabled(false)
        instance
    }

    fun getCoursesReference() = database.getReference("courses")
}