package com.example.tennislabspringboot.mappers

import com.example.tennislabspringboot.dto.pedido.PedidoDTOcreate
import com.example.tennislabspringboot.dto.pedido.PedidoDTOvisualize
import com.example.tennislabspringboot.models.pedido.Pedido
import com.example.tennislabspringboot.repositories.tarea.TareaRepository
import com.example.tennislabspringboot.repositories.user.UserRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PedidoMapper
@Autowired constructor(
    private val uRepo: UserRepository,
    private val tRepo: TareaRepository,
    private val tMapper: TareaMapper
) {
    /**
     * @author Daniel Rodriguez Muñoz
     * Mapeador de pedido a dto y de pedidoDTOCreate a Pedido
     */
    suspend fun toDTO(pedido: Pedido) : PedidoDTOvisualize {
        val tareaList = tRepo.findAll().filter { it.pedidoId == pedido.uuid }.toList()
        var precioTotal = 0.0
        tareaList.forEach { precioTotal += it.precio }

        return PedidoDTOvisualize(
            user = uRepo.findFirstByUuid(pedido.userId).toList().firstOrNull()?.toDTO(),
            state = pedido.state,
            fechaEntrada = pedido.fechaEntrada,
            fechaSalida = pedido.fechaSalida,
            topeEntrega = pedido.topeEntrega,
            tareas = tMapper.toDTO(tareaList),
            precio = precioTotal
        )
    }

    suspend fun toDTO(list: List<Pedido>) : List<PedidoDTOvisualize> {
        val res = mutableListOf<PedidoDTOvisualize>()
        list.forEach { res.add(toDTO(it)) }
        return res
    }
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

fun fromDTO(list: List<PedidoDTOcreate>) : List<Pedido> {
    val res = mutableListOf<Pedido>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}