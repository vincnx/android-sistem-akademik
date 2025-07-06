package com.vincnx.androidsistemakademik

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.di.AppContainer

class MyApplication: Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer()
    }
}