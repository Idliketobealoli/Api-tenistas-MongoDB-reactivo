package com.example.tennislabspringboot.models.pedido

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.*

/**
 * @author Iván Azagra Troya
 * Clase POKO Pedido
 */
@Document
data class Pedido(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val userId: UUID,
    val state: PedidoState,
    val fechaEntrada: LocalDate,
    val fechaSalida: LocalDate,
    val topeEntrega: LocalDate,
    var precio: Double
)