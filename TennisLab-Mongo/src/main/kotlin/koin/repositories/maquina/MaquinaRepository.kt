package koin.repositories.maquina

import koin.db.DBManager
import koin.models.maquina.Maquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.maquina.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import java.util.*

private val logger = KotlinLogging.logger{}

/**
 * @author Iván Azagra Troya
 * Repositorio de maquinas
 */
@Single
@Named("MaquinaRepository")
class MaquinaRepository: IMaquinaRepository<Id<Maquina>> {
    /**
     * Este método busca todos los maquinas en la base de datos
     */
    override suspend fun findAll(): Flow<Maquina> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }
        DBManager.database.getCollection<Maquina>().find().publisher.asFlow()
    }

    /**
     * Este método busca un maquina que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el maquina con el id.
     * @return maquina con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Maquina>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Maquina): Maquina = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Maquina>().save(entity).let { entity }
    }

    /**
     * Este método cambia el estado del maquina con el identificador a finalizado
     * @param id identificador de mongo
     * @return el maquina que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setInactive(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
            ?: return@withContext null
        val updated = Maquina(
            id = entity.id,
            uuid = entity.uuid,
            modelo = entity.modelo,
            marca = entity.marca,
            fechaAdquisicion = entity.fechaAdquisicion,
            numeroSerie = entity.numeroSerie,
            tipo = entity.tipo,
            activa = false,
            isManual = entity.isManual,
            maxTension = entity.maxTension,
            minTension = entity.minTension,
            measuresManeuverability = entity.measuresManeuverability,
            measuresRigidity = entity.measuresRigidity,
            measuresBalance = entity.measuresBalance
        )
        DBManager.database.getCollection<Maquina>().save(updated).let { updated }
    }

    /**
     * Este método borra el maquina del repositorio
     * @param id identificador de mongo
     * @return el maquina borrado
     */
    override suspend fun delete(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
        DBManager.database.getCollection<Maquina>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un maquina con el identificador pasado por parámetro
     * @param id identificador de mongo
     * @return el maquina que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Maquina>().findOneById(id)
    }
}