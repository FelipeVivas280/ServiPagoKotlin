package com.example.servipagokotlin.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servipagokotlin.data.PagoEntity
import com.example.servipagokotlin.data.ServicioEntity
import com.example.servipagokotlin.data.SesionUsuario
import com.example.servipagokotlin.ui.componentes.BarraNavegacionInferior
import com.example.servipagokotlin.ui.componentes.PantallaInferior
import com.example.servipagokotlin.viewmodel.PagosViewModel
import com.example.servipagokotlin.viewmodel.ServiciosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import servipagokotlin.shared.generated.resources.Res
import servipagokotlin.shared.generated.resources.agregar_logo
import servipagokotlin.shared.generated.resources.agua_logo
import servipagokotlin.shared.generated.resources.crunchyroll_logo
import servipagokotlin.shared.generated.resources.disneyplus_logo
import servipagokotlin.shared.generated.resources.fuego_logo
import servipagokotlin.shared.generated.resources.hbomax_logo
import servipagokotlin.shared.generated.resources.luz_logo
import servipagokotlin.shared.generated.resources.netflix_logo
import servipagokotlin.shared.generated.resources.perfil_logo
import servipagokotlin.shared.generated.resources.primevideo_logo
import servipagokotlin.shared.generated.resources.spotify_logo
import servipagokotlin.shared.generated.resources.wifi_logo
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ============================================================================
// Modelos y Servicio API REST Externa en Tiempo Real (USD / COP)
// ============================================================================

data class DolarInfo(
    val tasaActual: Double,
    val tasaAnterior: Double,
    val diferenciaCOP: Double,
    val cambioPorcentual: Double,
    val esAlza: Boolean,
    val afectacionTexto: String,
    val fuenteDatos: String
)

object ApiDolarService {
    suspend fun obtenerDatosDolar(): DolarInfo = withContext(Dispatchers.IO) {
        var tasaSpotInternet: Double? = null
        var trmRecienteInternet: Double? = null
        var trmAnteriorInternet: Double? = null

        try {
            val jsonTRM = descargarJson("https://www.datos.gov.co/resource/32sa-8pi3.json?\$limit=2&\$order=vigenciadesde%20DESC")
            if (jsonTRM != null) {
                val valores = Regex("\"valor\"\\s*:\\s*\"([0-9.]+)\"")
                    .findAll(jsonTRM)
                    .mapNotNull { it.groupValues[1].toDoubleOrNull() }
                    .toList()

                if (valores.size >= 2) {
                    trmRecienteInternet = valores[0]
                    trmAnteriorInternet = valores[1]
                } else if (valores.isNotEmpty()) {
                    trmRecienteInternet = valores[0]
                }
            }
        } catch (e: Exception) {
            println("Aviso consultando TRM histórica: ${e.message}")
        }

        try {
            val jsonSpot = descargarJson("https://open.er-api.com/v6/latest/USD")
            if (jsonSpot != null) {
                val matchCop = Regex("\"COP\"\\s*:\\s*([0-9.]+)").find(jsonSpot)
                tasaSpotInternet = matchCop?.groupValues?.get(1)?.toDoubleOrNull()
            }
        } catch (e: Exception) {
            println("Aviso consultando mercado spot USD: ${e.message}")
        }

        val actual: Double
        val anterior: Double
        val fuente: String

        if (tasaSpotInternet != null && trmRecienteInternet != null && trmAnteriorInternet != null) {
            actual = tasaSpotInternet
            anterior = if (abs(tasaSpotInternet - trmRecienteInternet) >= 0.05) trmRecienteInternet else trmAnteriorInternet
            fuente = "En vivo: Mercado Global + Superfinanciera CO"
        } else if (trmRecienteInternet != null && trmAnteriorInternet != null) {
            actual = trmRecienteInternet
            anterior = trmAnteriorInternet
            fuente = "En vivo: Datos Abiertos Colombia (TRM)"
        } else if (tasaSpotInternet != null) {
            actual = tasaSpotInternet
            anterior = trmRecienteInternet ?: tasaSpotInternet
            fuente = "En vivo: ExchangeRate API"
        } else {
            return@withContext DolarInfo(
                tasaActual = 4150.0,
                tasaAnterior = 4150.0,
                diferenciaCOP = 0.0,
                cambioPorcentual = 0.0,
                esAlza = true,
                afectacionTexto = "Sin conexión a internet: revisa tu red para ver datos en vivo.",
                fuenteDatos = "Modo sin conexión"
            )
        }

        val diferencia = actual - anterior
        val porcentaje = if (anterior > 0) (diferencia / anterior) * 100.0 else 0.0
        val subio = diferencia >= 0
        val pesosCambio = abs(diferencia).roundToInt()
        val porcentajeTxt = formatearDosDecimales(abs(porcentaje))

        val impacto = if (subio) {
            "El dólar subió $porcentajeTxt% (+$$pesosCambio COP): el peso colombiano pierde valor frente al dólar y encarece servicios digitales o importados."
        } else {
            "El dólar bajó $porcentajeTxt% (-$$pesosCambio COP): el peso colombiano se fortalece frente al dólar y alivia el costo en pesos de plataformas y tecnología."
        }

        DolarInfo(
            tasaActual = actual,
            tasaAnterior = anterior,
            diferenciaCOP = diferencia,
            cambioPorcentual = abs(porcentaje),
            esAlza = subio,
            afectacionTexto = impacto,
            fuenteDatos = fuente
        )
    }

    private fun descargarJson(urlString: String): String? {
        val conexion = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }
        return try {
            if (conexion.responseCode == 200) {
                conexion.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } finally {
            conexion.disconnect()
        }
    }
}

// ============================================================================
// Componente Visual del Indicador Económico y Conversor en Tiempo Real
// ============================================================================

@Composable
fun TarjetaIndicadorDolarView(totalPendienteCOP: Long) {
    var dolarInfo by remember {
        mutableStateOf(
            DolarInfo(
                tasaActual = 0.0,
                tasaAnterior = 0.0,
                diferenciaCOP = 0.0,
                cambioPorcentual = 0.0,
                esAlza = true,
                afectacionTexto = "Descargando cotización en tiempo real de internet...",
                fuenteDatos = "Conectando..."
            )
        )
    }

    LaunchedEffect(Unit) {
        dolarInfo = ApiDolarService.obtenerDatosDolar()
    }

    val montoBaseCOP = (if (totalPendienteCOP > 0L) totalPendienteCOP else 100_000L).toDouble()
    val conversionUSD = if (dolarInfo.tasaActual > 0.0) montoBaseCOP / dolarInfo.tasaActual else 0.0
    val impactoRecibosCOP = montoBaseCOP * (dolarInfo.cambioPorcentual / 100.0)
    val colorTendencia = if (dolarInfo.esAlza) Color(0xFFFF6B6B) else Color(0xFF3DD62E)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A2A2A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3DD62E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dólar vs Peso Colombiano",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dolarInfo.fuenteDatos,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colorTendencia.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (dolarInfo.esAlza) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = colorTendencia,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${if (dolarInfo.esAlza) "Subió" else "Bajó"} ${formatearDosDecimales(dolarInfo.cambioPorcentual)}%",
                    color = colorTendencia,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.4f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "DÓLAR HOY (1 USD)",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${formatearPesos(dolarInfo.tasaActual.roundToLong())} COP",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Anterior: ${formatearPesos(dolarInfo.tasaAnterior.roundToLong())} COP",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "MONEDA COLOMBIANA (COP)",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (dolarInfo.esAlza) "Peso se devaluó" else "Peso se fortaleció",
                    color = if (dolarInfo.esAlza) Color(0xFFFF9800) else Color(0xFF3DD62E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (dolarInfo.esAlza) "+" else "-"}${formatearPesos(abs(dolarInfo.diferenciaCOP).roundToLong())} COP por 1 USD",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF161616))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (totalPendienteCOP > 0L) "Conversión de tu Total Pendiente:" else "Conversión base ($100.000 COP):",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "${formatearPesos(montoBaseCOP.roundToLong())} COP = $${formatearDosDecimales(conversionUSD)} USD",
                    color = Color(0xFF29D9E8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Efecto de la variación de hoy:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "${if (dolarInfo.esAlza) "+" else "-"}${formatearPesos(impactoRecibosCOP.roundToLong())} COP en tus recibos",
                    color = colorTendencia,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = dolarInfo.afectacionTexto,
            color = Color.LightGray,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

// ============================================================================
// Vista Principal: Inicio
// ============================================================================

@OptIn(ExperimentalTime::class)
@Composable
fun Inicio(
    modifier: Modifier = Modifier,
    nombreUsuario: String = SesionUsuario.nombre,
    accionServicioAgregado: (nombre: String) -> Unit = {},
    accionInicio: () -> Unit = {},
    accionServicios: () -> Unit = {},
    accionPagos: () -> Unit = {},
    accionAjustes: () -> Unit = {},
    accionPerfil: () -> Unit = {},
    serviciosViewModel: ServiciosViewModel = viewModel { ServiciosViewModel() },
    pagosViewModel: PagosViewModel = viewModel { PagosViewModel() }
) {
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    val listaServicios by serviciosViewModel.servicios.collectAsState(initial = emptyList())
    val listaPagos by pagosViewModel.historialPagos.collectAsState(initial = emptyList<PagoEntity>())

    val nombreMostrado = nombreUsuario.ifBlank { "Usuario" }
    val pagosRealizadosCount = listaPagos.size
    val totalPendienteSuma = listaServicios.sumOf { it.monto }

    val serviciosProximos = remember(listaServicios) {
        listaServicios.sortedBy { it.vencimientoMillis }.take(3)
    }

    Surface(
        color = Color.Black,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 90.dp)
            ) {

                // ---------- Encabezado ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hola, $nombreMostrado",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                            .clickable(onClick = accionPerfil),
                        contentAlignment = Alignment.Center
                    ) {
                        val foto = SesionUsuario.fotoPerfil
                        if (foto != null) {
                            Image(
                                bitmap = foto,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(Res.drawable.perfil_logo),
                                contentDescription = "Perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- Tarjeta: Resumen del Mes ----------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF161616))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Resumen del Mes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Pendiente:", color = Color.LightGray, fontSize = 12.sp)
                            Text(
                                text = formatearPesos(totalPendienteSuma),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pagos Realizados :", color = Color.LightGray, fontSize = 12.sp)
                            Text(
                                text = pagosRealizadosCount.toString(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.height(112.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("80", color = Color.Gray, fontSize = 9.sp)
                            Text("60", color = Color.Gray, fontSize = 9.sp)
                            Text("40", color = Color.Gray, fontSize = 9.sp)
                            Text("20", color = Color.Gray, fontSize = 9.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(148.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            BarraResumen("Julio", 88.dp, Color(0xFFE53935))
                            BarraResumen("Agosto", 55.dp, Color(0xFFF6C453))
                            BarraResumen("Septiembre", 112.dp, Color(0xFF3DD62E))
                            BarraResumen("Octubre", 42.dp, Color(0xFF4E8DF5))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "PROXIMOS VENCIMIENTOS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ---------- Tarjetas de servicios dinámicas (Máximo 3) ----------
                if (serviciosProximos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161616))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay servicios próximos. ¡Agrega uno con el botón +!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val ahoraMillis = Clock.System.now().toEpochMilliseconds()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (servicio in serviciosProximos) {
                            val diffMillis = servicio.vencimientoMillis - ahoraMillis
                            val diasRestantes = (diffMillis / 86_400_000L).toInt()

                            val textoEstado = when {
                                diasRestantes < 0 -> "Vencido"
                                diasRestantes == 0 -> "Vence hoy"
                                diasRestantes == 1 -> "Vence mañana"
                                else -> "Vence en $diasRestantes días"
                            }

                            TarjetaServicio(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                icono = iconoParaServicioInicio(servicio),
                                titulo = servicio.nombre,
                                estado = textoEstado,
                                colorFondo = Color(0xFFE4483A),
                                textoBoton = "Ver más",
                                colorBoton = Color(0xFFB71C1C),
                                colorTextoBoton = Color.White,
                                accionBoton = accionServicios
                            )
                        }
                        repeat(3 - serviciosProximos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // ---------- Indicador API REST Externa en Tiempo Real (Dólar / COP) ----------
                Spacer(modifier = Modifier.height(24.dp))
                TarjetaIndicadorDolarView(totalPendienteCOP = totalPendienteSuma)
            }

            // ---------- Botón flotante (+) ----------
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 86.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF29D9E8))
                    .clickable { mostrarDialogo = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.agregar_logo),
                    contentDescription = "Agregar",
                    modifier = Modifier.size(26.dp)
                )
            }

            // ---------- Barra de navegación inferior ----------
            BarraNavegacionInferior(
                modifier = Modifier.align(Alignment.BottomCenter),
                pantallaActual = PantallaInferior.INICIO,
                accionInicio = accionInicio,
                accionServicios = accionServicios,
                accionPagos = accionPagos,
                accionAjustes = accionAjustes
            )

            // ---------- Diálogo de Nuevo Servicio Modificado ----------
            if (mostrarDialogo) {
                DialogoNuevoServicioModificado(
                    onCancelar = { mostrarDialogo = false },
                    onGuardarGenerado = { nombre, tipo, monto, dias ->
                        val vencimientoMillis = Clock.System.now().toEpochMilliseconds() + (dias.toLong() * 86_400_000L)

                        val nuevoServicio = ServicioEntity(
                            nombre = nombre,
                            tipo = tipo,
                            monto = monto,
                            vencimientoMillis = vencimientoMillis
                        )
                        serviciosViewModel.agregarServicio(nuevoServicio)
                        mostrarDialogo = false
                        accionServicioAgregado(nombre)
                    }
                )
            }
        }
    }
}

private fun iconoParaServicioInicio(servicio: ServicioEntity): DrawableResource {
    val tipo = servicio.tipo.lowercase()
    val nombre = servicio.nombre.lowercase()

    return when {
        nombre.contains("claro") || tipo.contains("internet") || tipo.contains("claro") -> Res.drawable.wifi_logo
        tipo.contains("luz") || nombre.contains("ceo") -> Res.drawable.luz_logo
        tipo.contains("agua") || nombre.contains("agua") || nombre.contains("gotica") -> Res.drawable.agua_logo
        tipo.contains("gas") || nombre.contains("gas") -> Res.drawable.fuego_logo
        nombre.contains("netflix") -> Res.drawable.netflix_logo
        nombre.contains("amazon") || nombre.contains("prime") -> Res.drawable.primevideo_logo
        nombre.contains("disney") -> Res.drawable.disneyplus_logo
        nombre.contains("max") -> Res.drawable.hbomax_logo
        nombre.contains("spotify") -> Res.drawable.spotify_logo
        nombre.contains("crunchyroll") -> Res.drawable.crunchyroll_logo
        else -> Res.drawable.wifi_logo
    }
}

private fun formatearPesos(monto: Long): String {
    val negativo = monto < 0
    val digitos = abs(monto).toString()
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

private fun formatearDosDecimales(valor: Double): String {
    val centesimas = (valor * 100.0).roundToLong()
    val entero = centesimas / 100
    val decimales = abs(centesimas % 100).toString().padStart(2, '0')
    return "$entero,$decimales"
}

// ============================================================================
// Diálogo de Nuevo Servicio
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevoServicioModificado(
    onCancelar: () -> Unit,
    onGuardarGenerado: (nombre: String, tipo: String, monto: Long, dias: Int) -> Unit
) {
    var categoriaSeleccionada by remember { mutableStateOf("Servicios públicos") }
    var expandidoCategoria by remember { mutableStateOf(false) }

    var numeroContrato by remember { mutableStateOf("") }

    var plataformaSeleccionada by remember { mutableStateOf("Netflix") }
    var expandidoPlataforma by remember { mutableStateOf(false) }
    var correoEntretenimiento by remember { mutableStateOf("") }

    var correoError by remember { mutableStateOf(false) }

    val listaCategorias = listOf("Servicios públicos", "Entretenimiento")
    val listaPlataformas = listOf("Netflix", "Amazon Prime", "Disney+", "Max", "Spotify", "Crunchyroll")

    val opcionesEmpresasPublicas = listOf(
        Pair("Claro", "Internet"),
        Pair("CEO", "Luz"),
        Pair("Gotica", "Agua"),
        Pair("Acueducto", "Agua"),
        Pair("Gas", "Gas")
    )

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Color(0xFF161616),
        title = {
            Text(
                text = "Nuevo servicio",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandidoCategoria,
                    onExpandedChange = { expandidoCategoria = !expandidoCategoria }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de servicio", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCategoria) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF29D9E8),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoCategoria,
                        onDismissRequest = { expandidoCategoria = false }
                    ) {
                        listaCategorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria) },
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    expandidoCategoria = false
                                }
                            )
                        }
                    }
                }

                if (categoriaSeleccionada == "Servicios públicos") {
                    OutlinedTextField(
                        value = numeroContrato,
                        onValueChange = { numeroContrato = it },
                        label = { Text("Número de contrato", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF29D9E8),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF29D9E8)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expandidoPlataforma,
                        onExpandedChange = { expandidoPlataforma = !expandidoPlataforma }
                    ) {
                        OutlinedTextField(
                            value = plataformaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Plataforma", color = Color.Gray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoPlataforma) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF29D9E8),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandidoPlataforma,
                            onDismissRequest = { expandidoPlataforma = false }
                        ) {
                            listaPlataformas.forEach { plataforma ->
                                DropdownMenuItem(
                                    text = { Text(plataforma) },
                                    onClick = {
                                        plataformaSeleccionada = plataforma
                                        expandidoPlataforma = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = correoEntretenimiento,
                        onValueChange = {
                            correoEntretenimiento = it
                            if (correoError) correoError = false
                        },
                        label = { Text("Correo de la cuenta", color = Color.Gray) },
                        singleLine = true,
                        isError = correoError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (correoError) Color.Red else Color(0xFF29D9E8),
                            unfocusedBorderColor = if (correoError) Color.Red else Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF29D9E8),
                            errorBorderColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (correoError) {
                        Text(
                            text = "Ingrese un correo válido (ej: @gmail.com, @unicauca.edu.co)",
                            color = Color.Red,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoriaSeleccionada == "Servicios públicos") {
                        if (numeroContrato.trim().isNotEmpty()) {
                            val seleccion = opcionesEmpresasPublicas.random()
                            val montoAleatorio = Random.nextLong(30_000L, 200_001L)
                            val diasAleatorios = Random.nextInt(1, 26)
                            onGuardarGenerado(seleccion.first, seleccion.second, montoAleatorio, diasAleatorios)
                        }
                    } else {
                        val correoLimpio = correoEntretenimiento.trim()
                        val partes = correoLimpio.split("@")
                        val esCorreoValido = partes.size == 2 &&
                                partes[0].isNotBlank() &&
                                partes[1].contains(".") &&
                                partes[1].substringAfterLast(".").length >= 2

                        if (esCorreoValido) {
                            correoError = false
                            val montoAleatorio = Random.nextLong(15_000L, 60_001L)
                            val diasAleatorios = Random.nextInt(1, 26)
                            onGuardarGenerado(plataformaSeleccionada, "Entretenimiento", montoAleatorio, diasAleatorios)
                        } else {
                            correoError = true
                        }
                    }
                }
            ) {
                Text(
                    text = "Guardar",
                    color = Color(0xFF29D9E8),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

// ============================================================================
// Componentes Visuales de Apoyo (Gráfica y Tarjetas)
// ============================================================================

@Composable
private fun BarraResumen(etiqueta: String, altura: Dp, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(altura)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = etiqueta,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TarjetaServicio(
    modifier: Modifier = Modifier,
    icono: DrawableResource,
    titulo: String,
    estado: String,
    colorFondo: Color,
    textoBoton: String,
    colorBoton: Color,
    colorTextoBoton: Color,
    accionBoton: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorFondo)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Text(
            text = titulo,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = estado,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colorBoton)
                .clickable(onClick = accionBoton)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = textoBoton,
                color = colorTextoBoton,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
