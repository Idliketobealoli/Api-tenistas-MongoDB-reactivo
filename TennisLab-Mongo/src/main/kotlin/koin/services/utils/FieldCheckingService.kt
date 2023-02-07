package koin.services.utils

import koin.dto.maquina.EncordadoraDTOcreate
import koin.dto.maquina.MaquinaDTOcreate
import koin.dto.maquina.PersonalizadoraDTOcreate
import koin.dto.pedido.PedidoDTOcreate
import koin.dto.producto.ProductoDTOcreate
import koin.dto.tarea.AdquisicionDTOcreate
import koin.dto.tarea.EncordadoDTOcreate
import koin.dto.tarea.PersonalizacionDTOcreate
import koin.dto.tarea.TareaDTOcreate
import koin.dto.turno.TurnoDTOcreate
import koin.dto.user.UserDTOcreate
import koin.repositories.user.UserRepositoryCached
import java.time.LocalDate

/**
 * @author Daniel Rodriguez Muñoz
 * Archivo con métodos para comprobar los datos de los DTOs
 */
fun fieldsAreIncorrect(user: UserDTOcreate): Boolean {
    return user.nombre.isBlank() || !user.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")) ||
            user.apellido.isBlank() || user.telefono.isBlank() || user.password.isBlank()
}
fun fieldsAreIncorrect(pedido: PedidoDTOcreate): Boolean {
    var res = fieldsAreIncorrect(pedido.user) ||
        pedido.fechaEntrada.isAfter(pedido.fechaSalida) ||
        pedido.fechaEntrada.isAfter(pedido.topeEntrega) ||
        fieldsAreIncorrect(pedido.tareas)
    pedido.tareas.forEach {
        when (it) {
            is EncordadoDTOcreate -> { if (it.pedidoId != pedido.uuid) res = true }
            is PersonalizacionDTOcreate -> { if (it.pedidoId != pedido.uuid) res = true }
            is AdquisicionDTOcreate -> { if (it.pedidoId != pedido.uuid) res = true }
        }
    }
    return res
}

fun fieldsAreIncorrect(tarea: TareaDTOcreate): Boolean {
    return when (tarea) {
        is EncordadoDTOcreate -> {
            fieldsAreIncorrect(tarea.raqueta) || tarea.tensionHorizontal < 0.0 ||
            tarea.tensionVertical < 0.0 || fieldsAreIncorrect(tarea.cordajeHorizontal) ||
            fieldsAreIncorrect(tarea.cordajeVertical)
        }
        is PersonalizacionDTOcreate -> {
            fieldsAreIncorrect(tarea.raqueta) || tarea.peso < 0 ||
            tarea.balance < 0.0 || tarea.rigidez < 0
        }
        is AdquisicionDTOcreate -> {
            fieldsAreIncorrect(tarea.raqueta) || tarea.precio < 0.0 ||
            fieldsAreIncorrect(tarea.productoAdquirido)
        }
    }
}

fun fieldsAreIncorrect(tareas: List<TareaDTOcreate>): Boolean {
    var res = false
    tareas.forEach { if (fieldsAreIncorrect(it)) res = true }
    return res
}

fun fieldsAreIncorrect(producto: ProductoDTOcreate): Boolean {
    return producto.marca.isBlank() || producto.modelo.isBlank() ||
            producto.precio < 0.0 || producto.stock < 0
}

fun fieldsAreIncorrect(maquina: MaquinaDTOcreate): Boolean {
    return when (maquina) {
        is EncordadoraDTOcreate -> {
            maquina.modelo.isBlank() || maquina.marca.isBlank() ||
            maquina.fechaAdquisicion.isAfter(LocalDate.now()) || maquina.numeroSerie.isBlank() ||
            maquina.minTension < 0.0 || maquina.maxTension < maquina.minTension
        }
        is PersonalizadoraDTOcreate -> {
            maquina.modelo.isBlank() || maquina.marca.isBlank() ||
            maquina.fechaAdquisicion.isAfter(LocalDate.now()) || maquina.numeroSerie.isBlank()
        }
    }
}

fun fieldsAreIncorrect(turno: TurnoDTOcreate): Boolean {
    return fieldsAreIncorrect(turno.worker) || fieldsAreIncorrect(turno.maquina) ||
            turno.horaFin.isBefore(turno.horaInicio) || fieldsAreIncorrect(turno.tarea1) ||
            (turno.tarea2 != null && fieldsAreIncorrect(turno.tarea2))
}

suspend fun checkUserEmailAndPhone(user: UserDTOcreate, uRepo: UserRepositoryCached): Boolean {
    val u = uRepo.findByUUID(user.uuid)
    val uMail = uRepo.findByEmail(user.email)
    val uPhone = uRepo.findByPhone(user.telefono)
    if ((u == null && uMail != null) || (u == null && uPhone != null) || u != null)
        return true
    return false
}