package koin.repositories.producto

import koin.db.DBManager
import koin.models.producto.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.producto.*
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
 * Repositorio de productos
 */
@Single
@Named("ProductoRepository")
class ProductoRepository: IProductoRepository<Id<Producto>> {
    /**
     * Este método busca todos los productos en la base de datos
     */
    override suspend fun findAll(): Flow<Producto> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Producto>().find().publisher.asFlow()
    }

    /**
     * Este método busca un producto que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el producto con el id.
     * @return producto con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Producto>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Producto): Producto = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Producto>().save(entity).let { entity }
    }

    /**
     * Este método sirve para bajar el stock del producto con el id pasado por parámetro
     * @param id de tipo UUID del producto
     * @return el producto con el stock actualizado
     */
    override suspend fun decreaseStock(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "decreaseStock($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
            ?: return@withContext null
        if (entity.stock == 0) return@withContext entity
        val updated = Producto(
            id = entity.id,
            uuid = entity.uuid,
            tipo = entity.tipo,
            marca = entity.marca,
            modelo = entity.modelo,
            precio = entity.precio,
            stock = entity.stock - 1
        )
        DBManager.database.getCollection<Producto>().save(updated).let { updated }
    }

    /**
     * Este método borra el producto del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el producto borrado
     */
    override suspend fun delete(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)

        DBManager.database.getCollection<Producto>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un producto con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el producto que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Producto>().findOneById(id)
    }
}