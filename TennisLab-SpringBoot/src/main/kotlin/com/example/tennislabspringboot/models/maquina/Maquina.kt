package com.example.tennislabspringboot.models.maquina

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.*

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de las maquinas.
 * La primera parte de los parametros son obligatorios y son la base de las maquinas,
 * los parametros opcionales son especificos para los distintos tipos de maquina.
 */
@Document
data class Maquina(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val tipo: TipoMaquina,
    val activa: Boolean,

    // esto es data para encordadoras
    val isManual: Boolean?,
    val maxTension: Double?,
    val minTension: Double?,

    // esto es data para personalizadoras
    val measuresManeuverability: Boolean?,
    val measuresRigidity: Boolean?,
    val measuresBalance: Boolean?
)