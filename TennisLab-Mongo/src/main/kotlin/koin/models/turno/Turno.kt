package koin.models.turno

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.LocalDateTimeSerializer
import koin.serializers.UUIDSerializer
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Iván Azagra
 * Clase POKO de Turno
 */
@Serializable
data class Turno(
    @BsonId @Contextual
    val id: Id<Turno> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val workerId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val maquinaId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaInicio: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaFin: LocalDateTime,
    val numPedidosActivos: Int,
    @Serializable(with = UUIDSerializer::class)
    val tarea1Id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val tarea2Id: UUID?,
    val finalizado: Boolean
)
