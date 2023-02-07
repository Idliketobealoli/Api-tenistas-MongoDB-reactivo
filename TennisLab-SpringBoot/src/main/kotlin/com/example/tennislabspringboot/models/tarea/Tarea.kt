package com.example.tennislabspringboot.models.tarea

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de las tareas.
 * La primera parte de los parametros son obligatorios y son la base de las tareas,
 * los parametros opcionales son especificos para los distintos tipos de tarea.
 */
@Document
data class Tarea(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val raquetaId: UUID,
    var precio: Double,
    val tipo: TipoTarea,
    @JsonProperty("completed")
    val finalizada: Boolean,
    val pedidoId: UUID,

    // esto es para adquisiciones
    val productoAdquiridoId: UUID?,

    // esto es para personalizaciones
    val peso: Int?,
    val balance: Double?,
    val rigidez: Int?,

    // esto es para encordados
    val tensionHorizontal: Double?,
    val cordajeHorizontalId: UUID?,
    val tensionVertical: Double?,
    val cordajeVerticalId: UUID?,
    val dosNudos: Boolean?
)