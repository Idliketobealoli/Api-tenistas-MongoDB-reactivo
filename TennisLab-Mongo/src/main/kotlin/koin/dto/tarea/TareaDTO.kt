package koin.dto.tarea

import koin.dto.producto.ProductoDTOcreate
import koin.dto.producto.ProductoDTOvisualize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.serializers.UUIDSerializer
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * DTOs de creación, visualización y lista de visualización
 */
@Serializable sealed interface TareaDTO
@Serializable sealed interface TareaDTOcreate : TareaDTO { fun fromDTO() : Tarea }
@Serializable
@SerialName("TareaDTOvisualize")
sealed interface TareaDTOvisualize : TareaDTO

@Serializable data class AdquisicionDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    var precio: Double,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val productoAdquirido: ProductoDTOcreate,
) : TareaDTOcreate {
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.ADQUISICION,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = productoAdquirido.uuid,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
}

@Serializable data class EncordadoDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val tensionHorizontal: Double,
    val cordajeHorizontal: ProductoDTOcreate,
    val tensionVertical: Double,
    val cordajeVertical: ProductoDTOcreate,
    val dosNudos: Boolean
) : TareaDTOcreate {
    val precio: Double = 15+cordajeHorizontal.precio+cordajeVertical.precio
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.ENCORDADO,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = null,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = tensionHorizontal,
        cordajeHorizontalId = cordajeHorizontal.uuid,
        tensionVertical = tensionVertical,
        cordajeVerticalId = cordajeVertical.uuid,
        dosNudos = dosNudos
    )
}

@Serializable data class PersonalizacionDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val peso: Int,
    val balance: Double,
    val rigidez: Int
) : TareaDTOcreate {
    val precio: Double = 60.0
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = null,
        peso = peso,
        balance = balance,
        rigidez = rigidez,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
}

@Serializable
@SerialName("AdquisicionDTOvisualize")
data class AdquisicionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val productoAdquirido: ProductoDTOvisualize?
) : TareaDTOvisualize

@Serializable
@SerialName("EncordadoDTOvisualize")
data class EncordadoDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val tensionHorizontal: Double,
    val cordajeHorizontal: ProductoDTOvisualize?,
    val tensionVertical: Double,
    val cordajeVertical: ProductoDTOvisualize?,
    val dosNudos: Boolean
) : TareaDTOvisualize

@Serializable
@SerialName("PersonalizacionDTOvisualize")
data class PersonalizacionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val peso: Int,
    val balance: Double,
    val rigidez: Int
) : TareaDTOvisualize

@Serializable
@SerialName("TareaDTOvisualizeList")
data class TareaDTOvisualizeList(val tareas: List<TareaDTOvisualize>)

@Serializable data class TareaDTOFromApi (
    @SerialName("mongo_id")
    val id: String? = null,
    val uuid: String? = null,
    val raquetaId: String? = null,
    var precio: Double? = null,
    val tipo: TipoTarea? = null,
    @SerialName("completed")
    val finalizada: Boolean? = null,
    val pedidoId: String? = null,
    val productoAdquiridoId: String? = null,
    val peso: Int? = null,
    val balance: Double? = null,
    val rigidez: Int? = null,
    val tensionHorizontal: Double? = null,
    val cordajeHorizontalId: String? = null,
    val tensionVertical: Double? = null,
    val cordajeVerticalId: String? = null,
    val dosNudos: Boolean? = null
)