package com.example.tennislabspringboot.dto.pedido

import com.example.tennislabspringboot.dto.tarea.TareaDTOcreate
import com.example.tennislabspringboot.dto.tarea.TareaDTOvisualize
import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOvisualize
import com.example.tennislabspringboot.models.pedido.PedidoState
import java.time.LocalDate
import java.util.*

/**
 * @author Iván Azagra Troya
 * DTOs de creación, visualización y la lista para visualización
 */
data class PedidoDTOcreate(
    val uuid: UUID = UUID.randomUUID(),
    val user: UserDTOcreate,
    val state: PedidoState = PedidoState.PROCESO,
    val fechaEntrada: LocalDate = LocalDate.now(),
    val fechaSalida: LocalDate = fechaEntrada,
    val topeEntrega: LocalDate = fechaSalida.plusMonths(1L),
    val tareas: List<TareaDTOcreate> = listOf()
)

data class PedidoDTOvisualize(
    val user: UserDTOvisualize?,
    val state: PedidoState,
    val fechaEntrada: LocalDate,
    val fechaSalida: LocalDate,
    val topeEntrega: LocalDate,
    val tareas: List<TareaDTOvisualize>,
    val precio: Double
)

data class PedidoDTOvisualizeList(val pedidos: List<PedidoDTOvisualize>)