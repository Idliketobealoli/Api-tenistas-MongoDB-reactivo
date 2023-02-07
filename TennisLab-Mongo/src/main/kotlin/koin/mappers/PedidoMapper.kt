package koin.mappers

import koin.dto.pedido.PedidoDTOcreate
import koin.dto.pedido.PedidoDTOvisualize
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import koin.models.pedido.Pedido
import koin.repositories.tarea.TareaRepository
import koin.repositories.user.UserRepository

private val uRepo = UserRepository()
private val tRepo = TareaRepository()

/**
 * @author Daniel Rodriguez Muñoz
 * Mapeador de pedido a dto y de pedidoDTOCreate a Pedido
 */
suspend fun Pedido.toDTO() : PedidoDTOvisualize {
    val tareaList = tRepo.findAll().filter { it.pedidoId == uuid }.toList()
    var precioTotal = 0.0
    tareaList.forEach { precioTotal += it.precio }

    return PedidoDTOvisualize(
        user = uRepo.findByUUID(userId)?.toDTO(),
        state = state,
        fechaEntrada = fechaEntrada,
        fechaSalida = fechaSalida,
        topeEntrega = topeEntrega,
        tareas = toDTO(tareaList),
        precio = precioTotal
    )
}

fun PedidoDTOcreate.fromDTO() : Pedido {
    var precioTotal = 0.0
    tareas.forEach {
        val t = it.fromDTO()
        precioTotal += t.precio
    }

    return Pedido(
        uuid = uuid,
        userId = user.uuid,
        state = state,
        fechaEntrada = fechaEntrada,
        fechaSalida = fechaSalida,
        topeEntrega = topeEntrega,
        precio = precioTotal
    )
}

suspend fun toDTO(list: List<Pedido>) : List<PedidoDTOvisualize> {
    val res = mutableListOf<PedidoDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<PedidoDTOcreate>) : List<Pedido> {
    val res = mutableListOf<Pedido>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}