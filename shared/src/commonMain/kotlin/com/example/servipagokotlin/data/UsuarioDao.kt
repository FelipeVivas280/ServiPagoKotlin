package com.example.servipagokotlin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE (LOWER(email) = LOWER(:identifier) OR LOWER(nombre) = LOWER(:identifier)) AND password = :password LIMIT 1")
    suspend fun login(identifier: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): UsuarioEntity?
}