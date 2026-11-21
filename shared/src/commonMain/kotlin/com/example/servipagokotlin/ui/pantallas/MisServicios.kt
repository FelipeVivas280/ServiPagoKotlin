package com.example.servipagokotlin.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servipagokotlin.data.ServicioEntity
import com.example.servipagokotlin.ui.componentes.BarraNavegacionInferior
import com.example.servipagokotlin.ui.componentes.DialogoNuevoServicio
import com.example.servipagokotlin.ui.componentes.PantallaInferior
import com.example.servipagokotlin.viewmodel.PagosViewModel
import com.example.servipagokotlin.viewmodel.ServiciosViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import servipagokotlin.shared.generated.resources.Res
import servipagokotlin.shared.generated.resources.agua_logo
import servipagokotlin.shared.generated.resources.crunchyroll_logo
import servipagokotlin.shared.generated.resources.disneyplus_logo
import servipagokotlin.shared.generated.resources.fuego_logo
import servipagokotlin.shared.generated.resources.hbomax_logo
import servipagokotlin.shared.generated.resources.luz_logo
import servipagokotlin.shared.generated.resources.netflix_logo
import servipagokotlin.shared.generated.resources.primevideo_logo
import servipagokotlin.shared.generated.resources.search_logo
import servipagokotlin.shared.generated.resources.spotify_logo
import servipagokotlin.shared.generated.resources.wifi_logo
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MisServicios(
    modifier: Modifier = Modifier,
    accionInicio: () -> Unit = {},
    accionServicios: () -> Unit = {},
    accionPagos: () -> Unit = {},
    accionAjustes: () -> Unit = {},
    serviciosViewModel: ServiciosViewModel = viewModel { ServiciosViewModel() },
    pagosViewModel: PagosViewModel = viewModel { PagosViewModel() }
) {
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
    val listaServicios by serviciosViewModel.servicios.collectAsState(initial = emptyList())

    Surface(
        color = Color.Black,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 90.dp)
            ) {
                // ---------- Barra de Búsqueda / Título superior ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.search_logo),
                            contentDescription = "Buscar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "Buscar servicio o convenio...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- Lista de Servicios ----------
                if (listaServicios.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay servicios registrados.\nToca el botón [+] para agregar uno.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val ahoraMillis = Clock.System.now().toEpochMilliseconds()
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(listaServicios) { servicio ->
                            val diffMillis = servicio.vencimientoMillis - ahoraMillis
                            val diasRestantes = (diffMillis / 86_400_000L).toInt()

                            val textoVencimiento = when {
                                diasRestantes < 0 -> "Vencido"
                                diasRestantes == 0 -> "Vence hoy"
                                diasRestantes == 1 -> "Vence mañana"
                                else -> "Vence en $diasRestantes días"
                            }

                            val (colorEstado, textoEstadoBoton) = when {
                                diasRestantes < 8 -> Pair(Color(0xFFE53935), "¡Pagar Ya!")
                                diasRestantes in 8..18 -> Pair(Color(0xFFFF9800), "Próximo")
                                else -> Pair(Color(0xFF3DD62E), "Al día")
                            }

                            val (iconoRes, colorFondoIcono) = when {
                                servicio.nombre.contains("Claro", ignoreCase = true) || servicio.tipo.contains("Claro", ignoreCase = true) ->
                                    Pair(Res.drawable.wifi_logo, Color(0xFF000000))
                                servicio.tipo.contains("Luz", ignoreCase = true) || servicio.nombre.contains("CEO", ignoreCase = true) ->
                                    Pair(Res.drawable.luz_logo, Color(0xFF000000))
                                servicio.tipo.contains("Agua", ignoreCase = true) || servicio.nombre.contains("Agua", ignoreCase = true) || servicio.nombre.contains("Gotica", ignoreCase = true) ->
                                    Pair(Res.drawable.agua_logo, Color(0xFF000000))
                                servicio.tipo.contains("Gas", ignoreCase = true) ->
                                    Pair(Res.drawable.fuego_logo, Color(0xFF000000))
                                servicio.nombre.contains("Netflix", ignoreCase = true) ->
                                    Pair(Res.drawable.netflix_logo, Color(0xFF000000))
                                servicio.nombre.contains("Amazon", ignoreCase = true) || servicio.nombre.contains("Prime", ignoreCase = true) ->
                                    Pair(Res.drawable.primevideo_logo, Color(0xFFFFFFFF))
                                servicio.nombre.contains("Disney", ignoreCase = true) ->
                                    Pair(Res.drawable.disneyplus_logo, Color(0xFF040714))
                                servicio.nombre.contains("Max", ignoreCase = true) ->
                                    Pair(Res.drawable.hbomax_logo, Color(0xFFFFFFFF))
                                servicio.nombre.contains("Spotify", ignoreCase = true) ->
                                    Pair(Res.drawable.spotify_logo, Color(0xFF000000))
                                servicio.nombre.contains("Crunchyroll", ignoreCase = true) ->
                                    Pair(Res.drawable.crunchyroll_logo, Color(0xFFFFFFFF))
                                else -> Pair(Res.drawable.wifi_logo, Color(0xFF000000))
                            }

                            val montoFormateado = formatearPesos(servicio.monto)

                            ItemServicioCard(
                                icono = iconoRes,
                                colorFondoIcono = colorFondoIcono,
                                nombre = servicio.nombre,
                                vencimientoTexto = textoVencimiento,
                                montoTexto = montoFormateado,
                                colorEstadoBadge = colorEstado,
                                textoBadge = textoEstadoBoton,
                                onPagarClick = {
                                    pagosViewModel.registrarPago(
                                        servicio = servicio.nombre,
                                        monto = montoFormateado
                                    )
                                    serviciosViewModel.eliminarServicio(servicio)
                                    accionPagos()
                                }
                            )
                        }
                    }
                }
            }

            // ---------- Barra de navegación inferior ----------
            BarraNavegacionInferior(
                modifier = Modifier.align(Alignment.BottomCenter),
                pantallaActual = PantallaInferior.SERVICIOS,
                accionInicio = accionInicio,
                accionServicios = accionServicios,
                accionPagos = accionPagos,
                accionAjustes = accionAjustes
            )

            // ---------- Diálogo para nuevo servicio ----------
            if (mostrarDialogo) {
                DialogoNuevoServicio(
                    onCancelar = { mostrarDialogo = false },
                    onGuardar = { nombre, tipo, monto, dias ->
                        val vencimientoMillis = Clock.System.now().toEpochMilliseconds() + (dias.toLong() * 86_400_000L)
                        val nuevoServicio = ServicioEntity(
                            nombre = nombre,
                            tipo = tipo.toString(),
                            monto = monto,
                            vencimientoMillis = vencimientoMillis
                        )
                        serviciosViewModel.agregarServicio(nuevoServicio)
                        mostrarDialogo = false
                    }
                )
            }
        }
    }
}

private fun formatearPesos(monto: Long): String {
    val negativo = monto < 0
    val digitos = kotlin.math.abs(monto).toString()
    val resultado = StringBuilder()
    var contador = 0
    for (i in digitos.length - 1 downTo 0) {
        resultado.append(digitos[i])
        contador++
        if (contador == 3 && i != 0) {
            resultado.append('.')
            contador = 0
        }
    }
    return (if (negativo) "-$" else "$") + resultado.reverse().toString()
}

@Composable
private fun ItemServicioCard(
    icono: DrawableResource,
    colorFondoIcono: Color,
    nombre: String,
    vencimientoTexto: String,
    montoTexto: String,
    colorEstadoBadge: Color,
    textoBadge: String,
    onPagarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161616))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colorFondoIcono),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(icono),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = nombre,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colorEstadoBadge)
                    .clickable(onClick = onPagarClick)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textoBadge,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(text = vencimientoTexto, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Total:", color = Color.Gray, fontSize = 13.sp)
                Text(text = montoTexto, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .clickable(onClick = onPagarClick)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pagar",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}