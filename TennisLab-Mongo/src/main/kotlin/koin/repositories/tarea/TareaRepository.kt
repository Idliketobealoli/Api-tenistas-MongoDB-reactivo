package koin.repositories.tarea

import koin.db.DBManager
import koin.models.tarea.Tarea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.tarea.*
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
 * Repositorio de tareas
 */
@Single
@Named("TareaRepository")
class TareaRepository: ITareaRepository<Id<Tarea>> {
    /**
     * Este método busca todos los tareas en la base de datos
     */
    override suspend fun findAll(): Flow<Tarea> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Tarea>().find().publisher.asFlow()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }
        
        DBManager.database.getCollection<Tarea>().save(entity).let { entity }
    }

    /**
     * Este método cambia el estado de la tarea con el identificador a finalizado
     * @param id identificador de mongo
     * @return el tarea que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizada(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "setFinalizada($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
            ?: return@withContext null
        val updated = Tarea(
            id = entity.id,
            uuid = entity.uuid,
            raquetaId = entity.raquetaId,
            precio = entity.precio,
            tipo = entity.tipo,
            finalizada = true,
            pedidoId = entity.pedidoId,
            productoAdquiridoId = entity.productoAdquiridoId,
            peso = entity.peso,
            balance = entity.balance,
            rigidez = entity.rigidez,
            tensionHorizontal = entity.tensionHorizontal,
            cordajeHorizontalId = entity.cordajeHorizontalId,
            tensionVertical = entity.tensionVertical,
            cordajeVerticalId = entity.cordajeVerticalId,
            dosNudos = entity.dosNudos
        )
        DBManager.database.getCollection<Tarea>().save(updated).let { updated }
    }

    /**
     * Este método borra el tarea del repositorio
     * @param id identificador de mongo
     * @return el tarea borrado
     */
    override suspend fun delete(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO){
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
        DBManager.database.getCollection<Tarea>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un tarea con el identificador pasado por parámetro
     * @param id identificador de mongo
     * @return el tarea que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Tarea>().findOneById(id)
    }

    /**
     * Este método busca un tarea que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el tarea con el id.
     * @return tarea con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Tarea>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }
}