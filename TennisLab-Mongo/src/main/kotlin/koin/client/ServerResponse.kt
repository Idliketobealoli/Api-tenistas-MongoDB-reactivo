package koin.client

import kotlinx.serialization.Serializable

/**
 * @author Daniel Rodriguez Muñoz
 * Clase respuesta del servidor
 */
@Serializable
data class ServerResponse<T: Any>(
    val code: Int,
    val data: T? = null,
    val message: String? = null
)
