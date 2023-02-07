package com.example.tennislabspringboot.dto.turno

import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOvisualize
import com.example.tennislabspringboot.dto.maquina.MaquinaDTOcreate
import com.example.tennislabspringboot.dto.maquina.MaquinaDTOvisualize
import com.example.tennislabspringboot.dto.tarea.TareaDTOcreate
import com.example.tennislabspringboot.dto.tarea.TareaDTOvisualize
import java.time.LocalDateTime
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * DTOs de creación, visualización y lista de visualización
 */
data class TurnoDTOcreate(
    val uuid: UUID = UUID.randomUUID(),
    val worker: UserDTOcreate,
    val maquina: MaquinaDTOcreate,
    val horaInicio: LocalDateTime = LocalDateTime.now(),
    val horaFin: LocalDateTime = horaInicio,
    val tarea1: TareaDTOcreate,
    val tarea2: TareaDTOcreate?,
    val finalizado: Boolean = false
)

data class TurnoDTOvisualize(
    val worker: UserDTOvisualize?,
    val maquina: MaquinaDTOvisualize?,
    val horaInicio: LocalDateTime,
    val horaFin: LocalDateTime,
    val numPedidosActivos: Int,
    val tarea1: TareaDTOvisualize?,
    val tarea2: TareaDTOvisualize?,
    val finalizado: Boolean
)

data class TurnoDTOvisualizeList(val turnos: List<TurnoDTOvisualize>)