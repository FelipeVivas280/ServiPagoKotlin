package com.example.servipagokotlin.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SesionUsuario {
    var nombre by mutableStateOf("Usuario")
    var email by mutableStateOf("")
    var telefono by mutableStateOf("+57 310 123 4567")
    var ciudad by mutableStateOf("Popayán, Cauca")

    private val _emailActualFlow = MutableStateFlow("")
    val emailActualFlow: StateFlow<String> = _emailActualFlow.asStateFlow()

    var fotoPerfil by mutableStateOf<ImageBitmap?>(null)

    fun iniciar(usuario: UsuarioEntity) {
        nombre = usuario.nombre
        email = usuario.email
        telefono = usuario.telefono
        ciudad = usuario.ciudad
        _emailActualFlow.value = usuario.email.trim().lowercase()
    }

    fun cerrar() {
        nombre = "Usuario"
        email = ""
        telefono = "+57 310 123 4567"
        ciudad = "Popayán, Cauca"
        fotoPerfil = null
        _emailActualFlow.value = ""
    }
}