package koin.models.tarea

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.UUIDSerializer
import java.util.UUID

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de las tareas.
 * La primera parte de los parametros son obligatorios y son la base de las tareas,
 * los parametros opcionales son especificos para los distintos tipos de tarea.
 */
@Serializable
data class Tarea(
    @BsonId @Contextual @SerialName("mongo_id")
    val id: Id<Tarea> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val raquetaId: UUID,
    var precio: Double,
    val tipo: TipoTarea,
    @SerialName("completed")
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    // esto es para adquisiciones
    @Serializable(with = UUIDSerializer::class)
    val productoAdquiridoId: UUID?,

    // esto es para personalizaciones
    val peso: Int?,
    val balance: Double?,
    val rigidez: Int?,

    // esto es para encordados
    val tensionHorizontal: Double?,
    @Serializable(with = UUIDSerializer::class)
    val cordajeHorizontalId: UUID?,
    val tensionVertical: Double?,
    @Serializable(with = UUIDSerializer::class)
    val cordajeVerticalId: UUID?,
    val dosNudos: Boolean?
)