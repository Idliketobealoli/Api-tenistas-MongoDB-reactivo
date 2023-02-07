package koin.models.pedido

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.LocalDateSerializer
import koin.serializers.UUIDSerializer
import java.time.LocalDate
import java.util.UUID

/**
 * @author Iván Azagra Troya
 * Clase POKO Pedido
 */
@Serializable
data class Pedido(
    @BsonId @Contextual
    val id: Id<Pedido> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val state: PedidoState,
    @Serializable(with = LocalDateSerializer::class)
    val fechaEntrada: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val fechaSalida: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val topeEntrega: LocalDate,
    var precio: Double
)