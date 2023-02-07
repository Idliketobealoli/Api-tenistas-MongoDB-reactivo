package koin.repositories.turno

import koin.db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.turno.Turno
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * @author Iván Azagra Troya
 * Repositorio de turnos
 */
@Single
@Named("TurnoRepository")
class TurnoRepository: ITurnoRepository<Id<Turno>> {
    /**
     * Este método busca todos los turnos en la base de datos
     */
    override suspend fun findAll(): Flow<Turno> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }
        DBManager.database.getCollection<Turno>().find().publisher.asFlow()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Turno): Turno = withContext(Dispatchers.IO){
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Turno>().save(entity).let { entity }
    }

    /**
     * Este método cambia el estado del turno con el identificador a finalizado
     * @param id identificador de mongo
     * @return el turno que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizado(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "setFinalizado($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
            ?: return@withContext null
        val updated = Turno(
            id = entity.id,
            uuid = entity.uuid,
            workerId = entity.workerId,
            maquinaId = entity.maquinaId,
            horaInicio = entity.horaInicio,
            horaFin = entity.horaFin,
            numPedidosActivos = entity.numPedidosActivos,
            tarea1Id = entity.tarea1Id,
            tarea2Id = entity.tarea2Id,
            finalizado = true
        )
        DBManager.database.getCollection<Turno>().save(updated).let { updated }
    }

    /**
     * Este método borra el turno del repositorio
     * @param id identificador de mongo
     * @return el turno borrado
     */
    override suspend fun delete(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
        DBManager.database.getCollection<Turno>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un turno con el identificador pasado por parámetro
     * @param id identificador de mongo
     * @return el turno que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Turno>().findOneById(id)
    }

    /**
     * Este método busca un turno que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el turno con el id.
     * @return turno con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Turno>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }
}