package koin.models.producto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.UUIDSerializer
import java.util.UUID

/**
 * @author Iván Azagra Troya
 * Clase POKO de Producto
 */
@Serializable
data class Producto(
    @BsonId @Contextual
    val id: Id<Producto> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    val precio: Double,
    val stock: Int
)