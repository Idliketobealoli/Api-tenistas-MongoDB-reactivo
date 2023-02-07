package koin.models.maquina

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.LocalDateSerializer
import koin.serializers.UUIDSerializer
import java.time.LocalDate
import java.util.*

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de las maquinas.
 * La primera parte de los parametros son obligatorios y son la base de las maquinas,
 * los parametros opcionales son especificos para los distintos tipos de maquina.
 */
@Serializable
data class Maquina(
    @BsonId @Contextual
    val id: Id<Maquina> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
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