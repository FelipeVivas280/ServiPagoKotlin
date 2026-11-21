package com.example.servipagokotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoServicio(val etiqueta: String) {
    AGUA("Agua"),
    LUZ("Luz"),
    GAS("Gas"),
    INTERNET("Internet"),
    OTRO("Otro")
}

@Entity(tableName = "servicios")
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioEmail: String = "",
    val nombre: String,
    val tipo: String,
    val monto: Long,
    val vencimientoMillis: Long,
    val pagado: Boolean = false
) {
    fun tipoServicio(): TipoServicio =
        TipoServicio.entries.firstOrNull { it.name == tipo } ?: TipoServicio.OTRO
}