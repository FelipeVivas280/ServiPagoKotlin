package com.example.servipagokotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pagos")
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioEmail: String = "",
    val servicio: String,
    val monto: String,
    val fecha: String,
    val estado: String = "Pagado"
)