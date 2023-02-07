package com.example.tennislabspringboot.dto.tarea

import com.example.tennislabspringboot.dto.producto.ProductoDTOcreate
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.models.tarea.TipoTarea
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * DTOs de creación, visualización y lista de visualización
 */
sealed interface TareaDTO
sealed interface TareaDTOcreate : TareaDTO { fun fromDTO() : Tarea }
sealed interface TareaDTOvisualize : TareaDTO

data class AdquisicionDTOcreate (
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    var precio: Double,
    val finalizada: Boolean = false,
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

data class EncordadoDTOcreate (
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
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

data class PersonalizacionDTOcreate (
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
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

data class AdquisicionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    val pedidoId: UUID,

    val productoAdquirido: ProductoDTOvisualize?
) : TareaDTOvisualize

data class EncordadoDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    val pedidoId: UUID,

    val tensionHorizontal: Double,
    val cordajeHorizontal: ProductoDTOvisualize?,
    val tensionVertical: Double,
    val cordajeVertical: ProductoDTOvisualize?,
    val dosNudos: Boolean
) : TareaDTOvisualize

data class PersonalizacionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    val pedidoId: UUID,

    val peso: Int,
    val balance: Double,
    val rigidez: Int
) : TareaDTOvisualize

data class TareaDTOvisualizeList(val tareas: List<TareaDTOvisualize>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TareaDTOFromApi (
    val id: String? = null,
    val uuid: String? = null,
    val raquetaId: String? = null,
    var precio: Double? = null,
    val tipo: TipoTarea? = null,
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
) : Serializable