package koin.repositories.pedido

import koin.db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.pedido.Pedido
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
 * Repositorio de pedidos
 */
@Single
@Named("PedidoRepository")
class PedidoRepository: IPedidoRepository<Id<Pedido>> {
    /**
     * Este método busca todos los pedidos en la base de datos
     */
    override suspend fun findAll(): Flow<Pedido> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Pedido>().find().publisher.asFlow()
    }

    /**
     * Este método busca un pedido que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el pedido con el id.
     * @return pedido con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Pedido>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Pedido): Pedido = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Pedido>().save(entity).let { entity }
    }

    /**
     * Este método borra el pedido del repositorio
     * @param id identificador de mongo
     * @return el pedido borrado
     */
    override suspend fun delete(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Pedido>().findOneById(id)
        DBManager.database.getCollection<Pedido>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un pedido con el identificador pasado por parámetro
     * @param id identificador de mongo
     * @return el pedido que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Pedido>().findOneById(id)
    }
}