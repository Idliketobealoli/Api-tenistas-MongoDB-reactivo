package com.example.tennislabspringboot.models.turno

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Iván Azagra
 * Clase POKO de Turno
 */
@Document
data class Turno(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val workerId: UUID,
    val maquinaId: UUID,
    val horaInicio: LocalDateTime,
    val horaFin: LocalDateTime,
    val numPedidosActivos: Int,
    val tarea1Id: UUID,
    val tarea2Id: UUID?,
    val finalizado: Boolean
)
