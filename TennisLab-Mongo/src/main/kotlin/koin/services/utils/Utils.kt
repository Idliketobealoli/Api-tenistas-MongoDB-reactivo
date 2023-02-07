package koin.services.utils

import java.util.*

/**
 * @author Iván Azagra
 * función para pasar uuid a cadena de texto
 */
fun String.toUUID(): UUID {
    return try {
        UUID.fromString(this.trim())
    }catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("UUID no válido, no está en formato UUID")
    }
}