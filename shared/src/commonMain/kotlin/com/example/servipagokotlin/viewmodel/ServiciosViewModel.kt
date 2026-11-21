package com.example.servipagokotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servipagokotlin.data.AppDatabase
import com.example.servipagokotlin.data.NubeRepositorio
import com.example.servipagokotlin.data.ServicioEntity
import com.example.servipagokotlin.data.SesionUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiciosViewModel : ViewModel() {

    private val servicioDao = AppDatabase.getInstance().servicioDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    val servicios: StateFlow<List<ServicioEntity>> = SesionUsuario.emailActualFlow
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList())
            else servicioDao.obtenerPorUsuario(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarServicio(servicio: ServicioEntity) {
        val emailActivo = SesionUsuario.email.trim().lowercase()
        if (emailActivo.isBlank()) return

        viewModelScope.launch {
            val servicioConUsuario = servicio.copy(usuarioEmail = emailActivo)
            servicioDao.insertar(servicioConUsuario)
            NubeRepositorio.subirServicio(servicioConUsuario)
        }
    }

    fun eliminarServicio(servicio: ServicioEntity) {
        viewModelScope.launch {
            servicioDao.eliminar(servicio)
            NubeRepositorio.eliminarServicioNube(servicio)
        }
    }
}