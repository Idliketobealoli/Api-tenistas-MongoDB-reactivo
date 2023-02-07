package koin.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase sellada es la base de las respuestas,
 * que seran devueltas por el Controller.
 */
@Serializable sealed class Response<T>

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase hereda de Response y la usara el Controller para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
@Serializable
@SerialName("ResponseSuccess")
class ResponseSuccess<T: Any>(val code: Int, val data: T) : Response<T>()

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase hereda de Response y la usara el Controller para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
@Serializable
@SerialName("ResponseError")
class ResponseError(val code: Int, val message: String?) : Response<@Contextual Nothing>()

