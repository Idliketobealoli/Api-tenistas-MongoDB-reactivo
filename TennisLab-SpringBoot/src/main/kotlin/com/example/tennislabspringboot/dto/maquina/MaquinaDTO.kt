package com.example.tennislabspringboot.dto.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import com.example.tennislabspringboot.models.maquina.TipoMaquina
import java.time.LocalDate
import java.util.*

/**
 * @author Iván Azagra Troya
 * Clases para el pasado de datos de los diferentes tipos de
 * máquinas para el paso de datos, creación de máquinas,
 * para visualización y los métodos para recuperar la Maquina del DTO
 */
sealed interface MaquinaDTO
sealed interface MaquinaDTOcreate : MaquinaDTO { fun fromDTO() : Maquina }
sealed interface MaquinaDTOvisualize : MaquinaDTO

data class EncordadoraDTOcreate (
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    val fechaAdquisicion: LocalDate = LocalDate.now(),
    val numeroSerie: String,
    val activa: Boolean = true,

    val isManual: Boolean,
    val maxTension: Double = 0.0,
    val minTension: Double = 0.0
) : MaquinaDTOcreate {
    override fun fromDTO() = Maquina (
        uuid = uuid,
        modelo = modelo,
        marca = marca,
        fechaAdquisicion = fechaAdquisicion,
        numeroSerie = numeroSerie,
        tipo = TipoMaquina.ENCORDADORA,
        activa = activa,
        isManual = isManual,
        maxTension = maxTension,
        minTension = minTension,
        measuresManeuverability = null,
        measuresRigidity = null,
        measuresBalance = null
    )
}

data class PersonalizadoraDTOcreate (
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    val fechaAdquisicion: LocalDate = LocalDate.now(),
    val numeroSerie: String,
    val activa: Boolean = true,

    val measuresManeuverability: Boolean,
    val measuresRigidity: Boolean,
    val measuresBalance: Boolean
) : MaquinaDTOcreate {
    override fun fromDTO() = Maquina (
        uuid = uuid,
        modelo = modelo,
        marca = marca,
        fechaAdquisicion = fechaAdquisicion,
        numeroSerie = numeroSerie,
        tipo = TipoMaquina.PERSONALIZADORA,
        activa = activa,
        isManual = null,
        maxTension = null,
        minTension = null,
        measuresManeuverability = measuresManeuverability,
        measuresRigidity = measuresRigidity,
        measuresBalance = measuresBalance
    )
}

data class EncordadoraDTOvisualize (
    val modelo: String,
    val marca: String,
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val activa: Boolean,

    val isManual: Boolean,
    val maxTension: Double,
    val minTension: Double
) : MaquinaDTOvisualize

data class PersonalizadoraDTOvisualize (
    val modelo: String,
    val marca: String,
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val activa: Boolean,

    val measuresManeuverability: Boolean,
    val measuresRigidity: Boolean,
    val measuresBalance: Boolean
) : MaquinaDTOvisualize

data class MaquinaDTOvisualizeList(val maquina: List<MaquinaDTOvisualize>)