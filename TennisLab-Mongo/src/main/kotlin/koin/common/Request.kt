package koin.common

import kotlinx.serialization.Serializable

/**
 * @author Daniel Rodriguez Muñoz
 * Clase para las peticiones al servidor con el token, código,
 * contenido y tipo
 */
@Serializable
data class Request(
    val token: String?,
    val code: Int?,
    val body: String?,
    val type: Type
) {
    /**
     * tipos de peticiones
     */
    enum class Type {
        LOGIN,REGISTER,REQUEST
    }
}