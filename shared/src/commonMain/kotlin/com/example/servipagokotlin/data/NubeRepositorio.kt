package com.example.servipagokotlin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object NubeRepositorio {
    private const val PROJECT_ID = "servipago-8ddcf"
    private const val BASE_URL =
        "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    private fun claveSegura(texto: String): String {
        return texto.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
    }

    private fun codificarSegmentoUrl(texto: String): String {
        return URLEncoder.encode(texto.trim().lowercase(), "UTF-8").replace("+", "%20")
    }

    private fun escaparJson(valor: String): String {
        return valor
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun extraerStringFirestore(json: String, campo: String): String? {
        val regex = Regex("\"$campo\"\\s*:\\s*\\{\\s*\"stringValue\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
        return regex.find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
    }

    private fun extraerIntFirestore(json: String, campo: String): Long? {
        val regex = Regex("\"$campo\"\\s*:\\s*\\{\\s*\"integerValue\"\\s*:\\s*\"?([0-9-]+)\"?")
        return regex.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }

    private fun extraerBoolFirestore(json: String, campo: String): Boolean {
        val regex = Regex("\"$campo\"\\s*:\\s*\\{\\s*\"booleanValue\"\\s*:\\s*(true|false)")
        return regex.find(json)?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: false
    }

    suspend fun verificarUsuarioExisteEnNube(email: String, nombre: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val emailLimpio = email.trim().lowercase()
                val docId = codificarSegmentoUrl(emailLimpio)
                val docDirecto = peticionHttp("$BASE_URL/usuarios/$docId", "GET", null)
                if (docDirecto != null && docDirecto.contains("\"fields\"")) {
                    return@withContext true
                }

                val json = peticionHttp("$BASE_URL/usuarios?pageSize=200", "GET", null)
                    ?: return@withContext false
                val nombreLimpio = nombre.trim().lowercase()

                val documentos = json.split("\"fields\":")
                for (i in 1 until documentos.size) {
                    val bloque = documentos[i]
                    val correoDoc = (extraerStringFirestore(bloque, "correo")
                        ?: extraerStringFirestore(bloque, "email"))?.lowercase()
                    val nombreDoc = extraerStringFirestore(bloque, "nombre")?.lowercase()
                    if (correoDoc == emailLimpio || (nombreLimpio.isNotBlank() && nombreDoc == nombreLimpio)) {
                        return@withContext true
                    }
                }
                false
            } catch (_: Exception) {
                false
            }
        }

    suspend fun subirUsuario(usuario: UsuarioEntity) = withContext(Dispatchers.IO) {
        try {
            val emailLimpio = usuario.email.trim().lowercase()
            val docId = codificarSegmentoUrl(emailLimpio)
            val ahora = System.currentTimeMillis()
            val json = """
                {
                  "fields": {
                    "nombre": { "stringValue": "${escaparJson(usuario.nombre.trim())}" },
                    "correo": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "email": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "password": { "stringValue": "${escaparJson(usuario.password)}" },
                    "telefono": { "stringValue": "${escaparJson(usuario.telefono)}" },
                    "ciudad": { "stringValue": "${escaparJson(usuario.ciudad)}" },
                    "creadoEn": { "integerValue": "$ahora" }
                  }
                }
            """.trimIndent()
            peticionHttp("$BASE_URL/usuarios/$docId", "PATCH", json)
        } catch (e: Exception) {
            println("Error subiendo usuario a Firestore: ${e.message}")
        }
    }

    suspend fun buscarUsuarioEnNube(identificador: String, password: String): UsuarioEntity? =
        withContext(Dispatchers.IO) {
            try {
                val idBuscado = identificador.trim().lowercase()
                val json = peticionHttp("$BASE_URL/usuarios?pageSize=200", "GET", null)
                    ?: return@withContext null

                val documentos = json.split("\"fields\":")
                for (i in 1 until documentos.size) {
                    val bloque = documentos[i]
                    val nombre = extraerStringFirestore(bloque, "nombre") ?: continue
                    val email = (extraerStringFirestore(bloque, "correo")
                        ?: extraerStringFirestore(bloque, "email")) ?: continue
                    val passGuardada = extraerStringFirestore(bloque, "password") ?: ""
                    val telefono = extraerStringFirestore(bloque, "telefono") ?: "+57 310 123 4567"
                    val ciudad = extraerStringFirestore(bloque, "ciudad") ?: "Popayán, Cauca"

                    val coincideUsuario = email.lowercase() == idBuscado || nombre.lowercase() == idBuscado
                    // Si el usuario viene de la app vieja y no tiene campo password, permite ingresar con cualquier clave;
                    // si tiene password registrado, exige que coincida.
                    val coincideClave = passGuardada.isEmpty() || passGuardada == password

                    if (coincideUsuario && coincideClave) {
                        return@withContext UsuarioEntity(
                            nombre = nombre,
                            email = email.lowercase(),
                            password = if (passGuardada.isNotEmpty()) passGuardada else password,
                            telefono = telefono,
                            ciudad = ciudad
                        )
                    }
                }
                null
            } catch (e: Exception) {
                println("Error buscando usuario en Firestore: ${e.message}")
                null
            }
        }

    suspend fun subirServicio(servicio: ServicioEntity) = withContext(Dispatchers.IO) {
        if (servicio.usuarioEmail.isBlank()) return@withContext
        try {
            val emailLimpio = servicio.usuarioEmail.trim().lowercase()
            val docId = claveSegura("${emailLimpio}_${servicio.nombre}_${servicio.vencimientoMillis}")
            val json = """
                {
                  "fields": {
                    "usuarioEmail": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "correo": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "nombre": { "stringValue": "${escaparJson(servicio.nombre)}" },
                    "tipo": { "stringValue": "${escaparJson(servicio.tipo)}" },
                    "monto": { "integerValue": "${servicio.monto}" },
                    "vencimientoMillis": { "integerValue": "${servicio.vencimientoMillis}" },
                    "pagado": { "booleanValue": ${servicio.pagado} }
                  }
                }
            """.trimIndent()
            peticionHttp("$BASE_URL/servicios/$docId", "PATCH", json)
        } catch (e: Exception) {
            println("Error subiendo servicio a Firestore: ${e.message}")
        }
    }

    suspend fun eliminarServicioNube(servicio: ServicioEntity) = withContext(Dispatchers.IO) {
        if (servicio.usuarioEmail.isBlank()) return@withContext
        try {
            val emailLimpio = servicio.usuarioEmail.trim().lowercase()
            val docId = claveSegura("${emailLimpio}_${servicio.nombre}_${servicio.vencimientoMillis}")
            peticionHttp("$BASE_URL/servicios/$docId", "DELETE", null)
        } catch (_: Exception) {
        }
    }

    suspend fun descargarServiciosUsuario(email: String, dao: ServicioDao) =
        withContext(Dispatchers.IO) {
            try {
                val emailLimpio = email.trim().lowercase()
                val json = peticionHttp("$BASE_URL/servicios?pageSize=300", "GET", null)
                    ?: return@withContext

                val listaDescargada = mutableListOf<ServicioEntity>()
                val documentos = json.split("\"fields\":")
                for (i in 1 until documentos.size) {
                    val bloque = documentos[i]
                    val dueño = (extraerStringFirestore(bloque, "usuarioEmail")
                        ?: extraerStringFirestore(bloque, "correo"))?.lowercase() ?: continue
                    if (dueño != emailLimpio) continue

                    val nombre = extraerStringFirestore(bloque, "nombre") ?: continue
                    val tipo = extraerStringFirestore(bloque, "tipo") ?: "Otro"
                    val monto = extraerIntFirestore(bloque, "monto") ?: 0L
                    val vencimiento = extraerIntFirestore(bloque, "vencimientoMillis") ?: 0L
                    val pagado = extraerBoolFirestore(bloque, "pagado")

                    listaDescargada.add(
                        ServicioEntity(
                            usuarioEmail = emailLimpio,
                            nombre = nombre,
                            tipo = tipo,
                            monto = monto,
                            vencimientoMillis = vencimiento,
                            pagado = pagado
                        )
                    )
                }

                dao.eliminarPorUsuario(emailLimpio)
                listaDescargada.forEach { dao.insertar(it) }
            } catch (e: Exception) {
                println("Error descargando servicios de Firestore: ${e.message}")
            }
        }

    suspend fun subirPago(pago: PagoEntity) = withContext(Dispatchers.IO) {
        if (pago.usuarioEmail.isBlank()) return@withContext
        try {
            val emailLimpio = pago.usuarioEmail.trim().lowercase()
            val docId = claveSegura("${emailLimpio}_${pago.servicio}_${pago.fecha}_${pago.monto}")
            val json = """
                {
                  "fields": {
                    "usuarioEmail": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "correo": { "stringValue": "${escaparJson(emailLimpio)}" },
                    "servicio": { "stringValue": "${escaparJson(pago.servicio)}" },
                    "monto": { "stringValue": "${escaparJson(pago.monto)}" },
                    "fecha": { "stringValue": "${escaparJson(pago.fecha)}" },
                    "estado": { "stringValue": "${escaparJson(pago.estado)}" }
                  }
                }
            """.trimIndent()
            peticionHttp("$BASE_URL/pagos/$docId", "PATCH", json)
        } catch (e: Exception) {
            println("Error subiendo pago a Firestore: ${e.message}")
        }
    }

    suspend fun descargarPagosUsuario(email: String, dao: PagoDao) =
        withContext(Dispatchers.IO) {
            try {
                val emailLimpio = email.trim().lowercase()
                val json = peticionHttp("$BASE_URL/pagos?pageSize=300", "GET", null)
                    ?: return@withContext

                val listaPagos = mutableListOf<PagoEntity>()
                val documentos = json.split("\"fields\":")
                for (i in 1 until documentos.size) {
                    val bloque = documentos[i]
                    val dueño = (extraerStringFirestore(bloque, "usuarioEmail")
                        ?: extraerStringFirestore(bloque, "correo"))?.lowercase() ?: continue
                    if (dueño != emailLimpio) continue

                    val servicio = extraerStringFirestore(bloque, "servicio") ?: continue
                    val monto = extraerStringFirestore(bloque, "monto") ?: "$0"
                    val fecha = extraerStringFirestore(bloque, "fecha") ?: "Hoy"
                    val estado = extraerStringFirestore(bloque, "estado") ?: "Pagado"

                    listaPagos.add(
                        PagoEntity(
                            usuarioEmail = emailLimpio,
                            servicio = servicio,
                            monto = monto,
                            fecha = fecha,
                            estado = estado
                        )
                    )
                }

                dao.eliminarPorUsuario(emailLimpio)
                listaPagos.forEach { dao.insertar(it) }
            } catch (e: Exception) {
                println("Error descargando pagos de Firestore: ${e.message}")
            }
        }

    private fun peticionHttp(urlStr: String, metodo: String, body: String?): String? {
        val conexion = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = if (metodo == "PATCH") "POST" else metodo
            if (metodo == "PATCH") {
                setRequestProperty("X-HTTP-Method-Override", "PATCH")
            }
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (body != null) {
                doOutput = true
            }
        }
        return try {
            if (body != null) {
                conexion.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = conexion.responseCode
            if (code in 200..299) {
                conexion.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conexion.errorStream?.bufferedReader()?.use { it.readText() }
                println("Firestore HTTP $code en $urlStr: $err")
                null
            }
        } finally {
            conexion.disconnect()
        }
    }
}