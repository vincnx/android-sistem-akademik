package com.vincnx.androidsistemakademik.di

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.data.repository.CourseRepo
import com.vincnx.androidsistemakademik.data.repository.EnrollmentRepo
import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient
import com.vincnx.androidsistemakademik.data.source.local.SessionManager

class AppContainer(private val context: Context) {
    val database: DatabaseClient by lazy {
        DatabaseClient
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    val enrollmentRepo: EnrollmentRepo by lazy {
        EnrollmentRepo(database.database)
    }

    val courseRepo: CourseRepo by lazy {
        CourseRepo(database.database)
    }
}