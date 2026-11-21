package com.example.servipagokotlin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {

    @Query("SELECT * FROM servicios WHERE LOWER(usuarioEmail) = LOWER(:email) ORDER BY vencimientoMillis ASC")
    fun obtenerPorUsuario(email: String): Flow<List<ServicioEntity>>

    @Insert
    suspend fun insertar(servicio: ServicioEntity)

    @Delete
    suspend fun eliminar(servicio: ServicioEntity)

    @Query("DELETE FROM servicios WHERE LOWER(usuarioEmail) = LOWER(:email)")
    suspend fun eliminarPorUsuario(email: String)
}