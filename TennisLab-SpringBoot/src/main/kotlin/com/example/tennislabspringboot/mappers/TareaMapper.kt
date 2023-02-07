package com.example.tennislabspringboot.mappers

import com.example.tennislabspringboot.dto.tarea.*
import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.models.tarea.TipoTarea
import com.example.tennislabspringboot.repositories.producto.ProductoRepository
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class TareaMapper
@Autowired constructor(
    private val pRepo: ProductoRepository
) {
    /**
     * @author Daniel Rodriguez Muñoz
     * Mapeador de Tarea a DTO, de Tarea a DTOApi y de TareaDTOfromApi a Tarea
     */
    suspend fun toDTO(tarea: Tarea) : TareaDTOvisualize {
        return when (tarea.tipo) {
            TipoTarea.ADQUISICION -> {
                AdquisicionDTOvisualize (
                    raqueta = pRepo.findFirstByUuid(tarea.raquetaId).toList().firstOrNull()?.toDTO(),
                    precio = tarea.precio,
                    finalizada = tarea.finalizada,
                    pedidoId = tarea.pedidoId,
                    productoAdquirido = pRepo.findFirstByUuid(tarea.productoAdquiridoId!!).toList().firstOrNull()?.toDTO()
                )
            }
            TipoTarea.ENCORDADO -> {
                EncordadoDTOvisualize (
                    raqueta = pRepo.findFirstByUuid(tarea.raquetaId).toList().firstOrNull()?.toDTO(),
                    precio = tarea.precio,
                    finalizada = tarea.finalizada,
                    pedidoId = tarea.pedidoId,
                    tensionHorizontal = tarea.tensionHorizontal!!,
                    cordajeHorizontal = pRepo.findFirstByUuid(tarea.cordajeHorizontalId!!).toList().firstOrNull()?.toDTO(),
                    tensionVertical = tarea.tensionVertical!!,
                    cordajeVertical = pRepo.findFirstByUuid(tarea.cordajeVerticalId!!).toList().firstOrNull()?.toDTO(),
                    dosNudos = tarea.dosNudos!!
                )
            }
            TipoTarea.PERSONALIZACION -> {
                PersonalizacionDTOvisualize (
                    raqueta = pRepo.findFirstByUuid(tarea.raquetaId).toList().firstOrNull()?.toDTO(),
                    precio = tarea.precio,
                    finalizada = tarea.finalizada,
                    pedidoId = tarea.pedidoId,
                    peso = tarea.peso!!,
                    balance = tarea.balance!!,
                    rigidez = tarea.rigidez!!
                )
            }
        }
    }
    suspend fun toDTO(list: List<Tarea>) : List<TareaDTOvisualize> {
        val res = mutableListOf<TareaDTOvisualize>()
        list.forEach { res.add(toDTO(it)) }
        return res
    }
}

fun Tarea.toDTOapi() = TareaDTOFromApi(
    id = id.toHexString(),
    uuid = uuid.toString(),
    raquetaId = raquetaId.toString(),
    precio = precio,
    tipo = tipo,
    finalizada = finalizada,
    pedidoId = pedidoId.toString(),
    productoAdquiridoId = productoAdquiridoId.toString(),
    peso = peso,
    balance = balance,
    rigidez = rigidez,
    tensionHorizontal = tensionHorizontal,
    cordajeHorizontalId = cordajeHorizontalId.toString(),
    tensionVertical = tensionVertical,
    cordajeVerticalId = cordajeVerticalId.toString(),
    dosNudos = dosNudos
)

fun TareaDTOFromApi.fromDTO() = Tarea (
    id = ObjectId.get(),
    uuid = uuid?.let {UUID.fromString(it)}
        .run { UUID.fromString("00000000-0000-0000-0000-000000000000") },
    raquetaId = raquetaId?.let { UUID.fromString(it) }
        .run {UUID.fromString("00000000-0000-0000-0000-000000000001") },
    precio = precio ?: 0.0,
    tipo = tipo ?: TipoTarea.ADQUISICION,
    finalizada = finalizada ?: true,
    pedidoId = pedidoId?.let { UUID.fromString(it) }
        .run { UUID.fromString("00000000-0000-0000-0000-000000000002") },
    productoAdquiridoId = productoAdquiridoId?.let { UUID.fromString(it) }
        .run { UUID.fromString("00000000-0000-0000-0000-000000000002") },
    peso = peso,
    balance = balance,
    rigidez = rigidez,
    tensionHorizontal = tensionHorizontal,
    cordajeHorizontalId = cordajeHorizontalId?.let { UUID.fromString(it) }.run { null },
    tensionVertical = tensionVertical,
    cordajeVerticalId = cordajeVerticalId?.let { UUID.fromString(it) }.run { null },
    dosNudos = dosNudos
)

fun fromDTO(list: List<TareaDTOcreate>) : List<Tarea> {
    val res = mutableListOf<Tarea>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}

fun fromAPItoTarea(list: List<TareaDTOFromApi>) : List<Tarea> {
    val res = mutableListOf<Tarea>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}