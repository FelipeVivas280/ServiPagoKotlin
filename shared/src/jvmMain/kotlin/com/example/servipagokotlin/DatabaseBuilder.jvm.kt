package com.example.servipagokotlin.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), "servipago_db.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}