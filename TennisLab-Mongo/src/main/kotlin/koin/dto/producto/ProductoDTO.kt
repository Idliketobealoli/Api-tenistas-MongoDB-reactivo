package koin.dto.producto

import kotlinx.serialization.Serializable
import koin.models.producto.TipoProducto
import koin.serializers.UUIDSerializer
import kotlinx.serialization.SerialName
import java.util.*

/**
 * @author Iván Azagra Troya
 * DTOs de creación, visualización y lista de visualización
 */
@Serializable
data class ProductoDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int = 0
)

@Serializable
@SerialName("ProductoDTOvisualize")
data class ProductoDTOvisualize(
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int
)

@Serializable
@SerialName("ProductoDTOvisualizeList")
data class ProductoDTOvisualizeList(val productos: List<ProductoDTOvisualize>)