package koin.mappers

import koin.dto.tarea.*
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import koin.repositories.producto.ProductoRepository
import org.litote.kmongo.util.idValue
import java.util.*

private val pRepo = ProductoRepository()

/**
 * @author Daniel Rodriguez Muñoz
 * Mapeador de Tarea a DTO, de Tarea a DTOApi y de TareaDTOfromApi a Tarea
 */
suspend fun Tarea.toDTO() : TareaDTOvisualize {
    return when (tipo) {
        TipoTarea.ADQUISICION -> {
            AdquisicionDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                productoAdquirido = pRepo.findByUUID(productoAdquiridoId!!)?.toDTO()
            )
        }
        TipoTarea.ENCORDADO -> {
            EncordadoDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                tensionHorizontal = tensionHorizontal!!,
                cordajeHorizontal = pRepo.findByUUID(cordajeHorizontalId!!)?.toDTO(),
                tensionVertical = tensionVertical!!,
                cordajeVertical = pRepo.findByUUID(cordajeVerticalId!!)?.toDTO(),
                dosNudos = dosNudos!!
            )
        }
        TipoTarea.PERSONALIZACION -> {
            PersonalizacionDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                peso = peso!!,
                balance = balance!!,
                rigidez = rigidez!!
            )
        }
    }
}

fun Tarea.toDTOapi() = TareaDTOFromApi(
    id = id.idValue.toString(),
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
    id = id?.toId() ?: newId(),
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

suspend fun toDTO(list: List<Tarea>) : List<TareaDTOvisualize> {
    val res = mutableListOf<TareaDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

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