package com.example.servipagokotlin.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import servipagokotlin.shared.generated.resources.Res
import servipagokotlin.shared.generated.resources.llave_logo
import servipagokotlin.shared.generated.resources.usuario_logo

@Composable
fun IniciarSesion(
    modifier: Modifier = Modifier,
    onLoginExitoso: () -> Unit = {},
    onIrARegistro: () -> Unit = {}
) {
    var identificador by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

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
                text = "Iniciar Sesión",
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
                    value = identificador,
                    onValueChange = { identificador = it; mensajeError = null },
                    placeholder = { Text("Correo o Nombre de Usuario", color = Color.LightGray) },
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
                    value = password,
                    onValueChange = { password = it; mensajeError = null },
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

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    enabled = !cargando,
                    onClick = {
                        if (identificador.isBlank() || password.isBlank()) {
                            mensajeError = "Por favor completa todos los campos"
                            return@Button
                        }

                        cargando = true
                        scope.launch {
                            try {
                                val database = AppDatabase.getInstance()
                                val dao = database.usuarioDao()

                                // 1. Buscar primero en la base local Room
                                var usuarioEncontrado = dao.login(identificador.trim(), password)

                                // 2. Si no existe localmente (ej. creado en Desktop y abierto en Android), buscar en la nube
                                if (usuarioEncontrado == null) {
                                    val desdeNube = NubeRepositorio.buscarUsuarioEnNube(identificador.trim(), password)
                                    if (desdeNube != null) {
                                        dao.registrarUsuario(desdeNube)
                                        usuarioEncontrado = desdeNube
                                    }
                                }

                                if (usuarioEncontrado != null) {
                                    SesionUsuario.iniciar(usuarioEncontrado)
                                    // Sincronizar los servicios y pagos exclusivos de este usuario
                                    NubeRepositorio.descargarServiciosUsuario(usuarioEncontrado.email, database.servicioDao())
                                    NubeRepositorio.descargarPagosUsuario(usuarioEncontrado.email, database.pagoDao())
                                    cargando = false
                                    onLoginExitoso()
                                } else {
                                    cargando = false
                                    mensajeError = "Usuario, correo o contraseña incorrectos"
                                }
                            } catch (e: Exception) {
                                cargando = false
                                mensajeError = "Error al iniciar sesión"
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
                        text = if (cargando) "Verificando..." else "Entrar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val textoRegistro = buildAnnotatedString {
                append("¿No tienes cuenta? ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("Regístrate aquí")
                }
            }
            Text(
                text = textoRegistro,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(onClick = onIrARegistro)
            )
        }
    }
}