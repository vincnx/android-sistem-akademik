package com.vincnx.androidsistemakademik.di

import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient

class AppContainer {
    val database = DatabaseClient
}