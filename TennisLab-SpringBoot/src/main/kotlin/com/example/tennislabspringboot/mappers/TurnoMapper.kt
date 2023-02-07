package com.example.tennislabspringboot.mappers

import com.example.tennislabspringboot.dto.turno.TurnoDTOcreate
import com.example.tennislabspringboot.dto.turno.TurnoDTOvisualize
import com.example.tennislabspringboot.models.pedido.PedidoState
import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.repositories.maquina.MaquinaRepository
import com.example.tennislabspringboot.repositories.pedido.PedidoRepository
import com.example.tennislabspringboot.repositories.tarea.TareaRepository
import com.example.tennislabspringboot.repositories.user.UserRepository
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TurnoMapper
@Autowired constructor(
    private val uRepo: UserRepository,
    private val mRepo: MaquinaRepository,
    private val tRepo: TareaRepository,
    private val pRepo: PedidoRepository,
    private val tMapper: TareaMapper
) {
    /**
     * @author Iván Azagra Troya
     * Mapeador de Turno a TurnoDTOVisualize y de TurnoDTOCreate a Turno
     */
    suspend fun toDTO(turno: Turno) : TurnoDTOvisualize {
        val t1 = tRepo.findFirstByUuid(turno.tarea1Id).toList().firstOrNull()
        val t2 = turno.tarea2Id?.let { tRepo.findFirstByUuid(it).toList().firstOrNull() }
        return TurnoDTOvisualize (
            worker = uRepo.findFirstByUuid(turno.workerId).toList().firstOrNull()?.toDTO(),
            maquina = mRepo.findFirstByUuid(turno.maquinaId).toList().firstOrNull()?.toDTO(),
            horaInicio = turno.horaInicio,
            horaFin = turno.horaFin,
            numPedidosActivos = turno.numPedidosActivos,
            tarea1 = t1?.let { tMapper.toDTO(it) },
            tarea2 = t2?.let { tMapper.toDTO(it) },
            finalizado = turno.finalizado
        )
    }

    suspend fun fromDTO(turno: TurnoDTOcreate) : Turno {
        val pedido1 = pRepo.findFirstByUuid(turno.tarea1.fromDTO().pedidoId).toList().firstOrNull()
        val pedido2 = turno.tarea2?.fromDTO()?.pedidoId?.let { pRepo.findFirstByUuid(it).toList().firstOrNull() }
        var num = 0
        if (pedido1 != null && pedido1.state == PedidoState.PROCESO) num++
        if (pedido1 != null && pedido2 != null &&
            pedido2.state == PedidoState.PROCESO && pedido1.uuid != pedido2.uuid) num++

        return Turno (
            uuid = turno.uuid,
            workerId = turno.worker.uuid,
            maquinaId = turno.maquina.fromDTO().uuid,
            horaInicio = turno.horaInicio,
            horaFin = turno.horaFin,
            tarea1Id = turno.tarea1.fromDTO().uuid,
            tarea2Id = turno.tarea2?.fromDTO()?.uuid,
            finalizado = turno.finalizado,
            numPedidosActivos = num
        )
    }

    suspend fun toDTO(list: List<Turno>) : List<TurnoDTOvisualize> {
        val res = mutableListOf<TurnoDTOvisualize>()
        list.forEach { res.add(toDTO(it)) }
        return res
    }

    suspend fun fromDTO(list: List<TurnoDTOcreate>) : List<Turno> {
        val res = mutableListOf<Turno>()
        list.forEach { res.add(fromDTO(it)) }
        return res
    }
}