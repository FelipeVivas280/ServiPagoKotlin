package com.example.servipagokotlin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {

    @Query("SELECT * FROM pagos WHERE LOWER(usuarioEmail) = LOWER(:email) ORDER BY id DESC")
    fun obtenerPorUsuario(email: String): Flow<List<PagoEntity>>

    @Insert
    suspend fun insertar(pago: PagoEntity)

    @Query("DELETE FROM pagos WHERE LOWER(usuarioEmail) = LOWER(:email)")
    suspend fun eliminarPorUsuario(email: String)
}