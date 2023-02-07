package com.example.tennislabspringboot.models.producto

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * @author Iván Azagra Troya
 * Clase POKO de Producto
 */
@Document
data class Producto(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    val precio: Double,
    val stock: Int
)