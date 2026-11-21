package com.example.servipagokotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servipagokotlin.data.AppDatabase
import com.example.servipagokotlin.data.NubeRepositorio
import com.example.servipagokotlin.data.PagoEntity
import com.example.servipagokotlin.data.SesionUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PagosViewModel : ViewModel() {

    private val dao = AppDatabase.getInstance().pagoDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historialPagos: StateFlow<List<PagoEntity>> = SesionUsuario.emailActualFlow
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList())
            else dao.obtenerPorUsuario(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun registrarPago(servicio: String, monto: String, fecha: String = "Hoy") {
        val emailActivo = SesionUsuario.email.trim().lowercase()
        if (emailActivo.isBlank()) return

        viewModelScope.launch {
            val pago = PagoEntity(
                usuarioEmail = emailActivo,
                servicio = servicio,
                monto = monto,
                fecha = fecha
            )
            dao.insertar(pago)
            NubeRepositorio.subirPago(pago)
        }
    }
}