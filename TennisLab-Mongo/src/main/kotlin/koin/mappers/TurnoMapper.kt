package koin.mappers

import koin.dto.turno.TurnoDTOcreate
import koin.dto.turno.TurnoDTOvisualize
import koin.models.pedido.PedidoState
import koin.models.turno.Turno
import koin.repositories.maquina.MaquinaRepository
import koin.repositories.pedido.PedidoRepository
import koin.repositories.tarea.TareaRepository
import koin.repositories.user.UserRepository

private val uRepo = UserRepository()
private val mRepo = MaquinaRepository()
private val tRepo = TareaRepository()
private val pRepo = PedidoRepository()

/**
 * @author Iván Azagra Troya
 * Mapeador de Turno a TurnoDTOVisualize y de TurnoDTOCreate a Turno
 */
suspend fun Turno.toDTO() = TurnoDTOvisualize (
    worker = uRepo.findByUUID(workerId)?.toDTO(),
    maquina = mRepo.findByUUID(maquinaId)?.toDTO(),
    horaInicio = horaInicio,
    horaFin = horaFin,
    numPedidosActivos = numPedidosActivos,
    tarea1 = tRepo.findByUUID(tarea1Id)?.toDTO(),
    tarea2 = tarea2Id?.let { tRepo.findByUUID(it)?.toDTO() },
    finalizado = finalizado
)

suspend fun TurnoDTOcreate.fromDTO() : Turno {
    val pedido1 = pRepo.findByUUID(tarea1.fromDTO().pedidoId)
    val pedido2 = tarea2?.fromDTO()?.pedidoId?.let { pRepo.findByUUID(it) }
    var num = 0
    if (pedido1 != null && pedido1.state == PedidoState.PROCESO) num++
    if (pedido1 != null && pedido2 != null &&
        pedido2.state == PedidoState.PROCESO && pedido1.uuid != pedido2.uuid) num++

    return Turno (
        uuid = uuid,
        workerId = worker.uuid,
        maquinaId = maquina.fromDTO().uuid,
        horaInicio = horaInicio,
        horaFin = horaFin,
        tarea1Id = tarea1.fromDTO().uuid,
        tarea2Id = tarea2?.fromDTO()?.uuid,
        finalizado = finalizado,
        numPedidosActivos = num
    )
}

suspend fun toDTO(list: List<Turno>) : List<TurnoDTOvisualize> {
    val res = mutableListOf<TurnoDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

suspend fun fromDTO(list: List<TurnoDTOcreate>) : List<Turno> {
    val res = mutableListOf<Turno>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}