package com.example.servipagokotlin.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [PagoEntity::class, ServicioEntity::class, UsuarioEntity::class],
    version = 4,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pagoDao(): PagoDao
    abstract fun servicioDao(): ServicioDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return INSTANCE ?: getDatabaseBuilder()
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
        }
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>