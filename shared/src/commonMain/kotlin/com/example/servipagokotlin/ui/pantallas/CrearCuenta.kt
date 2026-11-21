package com.example.servipagokotlin.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servipagokotlin.data.AppDatabase
import com.example.servipagokotlin.data.NubeRepositorio
import com.example.servipagokotlin.data.SesionUsuario
import com.example.servipagokotlin.data.UsuarioEntity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import servipagokotlin.shared.generated.resources.Res
import servipagokotlin.shared.generated.resources.email_logo
import servipagokotlin.shared.generated.resources.llave_logo
import servipagokotlin.shared.generated.resources.usuario_logo

@Composable
fun CrearCuenta(
    modifier: Modifier = Modifier,
    accionIniciarSesion: () -> Unit = {},
    accionTerminos: () -> Unit = {},
    accionRegistroExitoso: () -> Unit = {}
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmarContrasena by rememberSaveable { mutableStateOf("") }
    var aceptaTerminos by rememberSaveable { mutableStateOf(false) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Surface(
        color = Color.Black,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear Cuenta",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Ingresa tus Datos",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; mensajeError = null },
                    placeholder = { Text("Nombre", color = Color.LightGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.usuario_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF4A4A4A),
                        focusedContainerColor = Color(0xFF4A4A4A),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; mensajeError = null },
                    placeholder = { Text("Email", color = Color.LightGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.email_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF4A4A4A),
                        focusedContainerColor = Color(0xFF4A4A4A),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it; mensajeError = null },
                    placeholder = { Text("Contraseña", color = Color.LightGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.llave_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF4A4A4A),
                        focusedContainerColor = Color(0xFF4A4A4A),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it; mensajeError = null },
                    placeholder = { Text("Confirmar Contraseña", color = Color.LightGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.llave_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF4A4A4A),
                        focusedContainerColor = Color(0xFF4A4A4A),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = aceptaTerminos,
                        onCheckedChange = { aceptaTerminos = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val texto = buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append("Terminos y Condiciones")
                        }
                        append(" y la ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append("Politica de Privacidad")
                        }
                    }
                    Text(
                        text = texto,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { accionTerminos() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    enabled = !cargando,
                    onClick = {
                        val nombreLimpio = nombre.trim()
                        val emailLimpio = email.trim().lowercase()

                        if (nombreLimpio.isBlank() || emailLimpio.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
                            mensajeError = "Por favor completa todos los campos"
                            return@Button
                        }
                        if (!emailLimpio.contains("@") || !emailLimpio.contains(".")) {
                            mensajeError = "Ingresa un correo electrónico válido"
                            return@Button
                        }
                        if (contrasena != confirmarContrasena) {
                            mensajeError = "Las contraseñas no coinciden"
                            return@Button
                        }
                        if (!aceptaTerminos) {
                            mensajeError = "Debes aceptar términos y condiciones"
                            return@Button
                        }

                        cargando = true
                        scope.launch {
                            try {
                                val database = AppDatabase.getInstance()
                                val dao = database.usuarioDao()

                                // 1. Validar duplicados en base local y en la nube
                                val existeLocal = dao.obtenerPorEmail(emailLimpio) != null ||
                                        dao.obtenerPorNombre(nombreLimpio) != null
                                val existeNube = NubeRepositorio.verificarUsuarioExisteEnNube(emailLimpio, nombreLimpio)

                                if (existeLocal || existeNube) {
                                    mensajeError = "Este correo o nombre de usuario ya está registrado"
                                    cargando = false
                                    return@launch
                                }

                                // 2. Crear usuario nuevo (empieza con 0 servicios y 0 pagos)
                                val nuevoUsuario = UsuarioEntity(
                                    nombre = nombreLimpio,
                                    email = emailLimpio,
                                    password = contrasena
                                )
                                dao.registrarUsuario(nuevoUsuario)
                                NubeRepositorio.subirUsuario(nuevoUsuario)

                                // 3. Iniciar sesión automáticamente con la cuenta limpia
                                SesionUsuario.iniciar(nuevoUsuario)
                                cargando = false
                                accionRegistroExitoso()
                            } catch (e: Exception) {
                                cargando = false
                                mensajeError = "Error al guardar: ${e.message ?: "fallo en base de datos"}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (cargando) "Creando..." else "Crear Cuenta",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val textoLogin = buildAnnotatedString {
                append("¿Ya tienes cuenta? ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("Iniciar Sesion")
                }
            }
            Text(
                text = textoLogin,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(onClick = accionIniciarSesion)
            )
        }
    }
}