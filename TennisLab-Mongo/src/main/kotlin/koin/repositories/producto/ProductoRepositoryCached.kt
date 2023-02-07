package koin.repositories.producto

import koin.mappers.toDTO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import koin.models.producto.Producto
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.producto.IProductoCache
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de productos cacheados
 * @constructor Hace uso de la interfaz del repositorio de productos
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de productos cacheados
 */
@Single
@Named("ProductoRepositoryCached")
class ProductoRepositoryCached(
    @Named("ProductoRepository")
    private val repo: IProductoRepository<Id<Producto>>,
    private val cache: IProductoCache
): IProductoRepository<Id<Producto>> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Producto>()

    init { refreshCache() }

    /**
     * Método para refrescar la caché, en caso de que refreshJob sea nulo se cancela el Job
     */
    private fun refreshCache() {
        if (refreshJob != null) refreshJob?.cancel()

        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if(listSearches.isNotEmpty()) {
                    listSearches.forEach {
                        cache.cache.put(it.uuid, it)
                    }
                }

                delay(cache.refreshTime)
            }
        }
    }

    /**
     * Este método busca todos los productos y los emite cada segundo en forma
     * de lista de productos
     */
    suspend fun findAllAsFlow() = flow {
        do {
            emit(toDTO(repo.findAll().toList()))
            delay(1000)
        } while (true)
    }

    /**
     * Este método busca todos los productos y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<Producto> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    /**
     * Este método busca un producto que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return producto con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de productos y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): Producto? = withContext(Dispatchers.IO) {
        var result: Producto? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    /**
     * Este método guarda la entidad pasada a listSearches y también en el repositorio
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Producto): Producto = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    /**
     * Este método decrece la cantidad de stock del producto con el identificador pasado
     * y lo almacena en la caché
     * @param id identificador de mongo
     * @return el producto actualizado
     */
    override suspend fun decreaseStock(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        val result = repo.decreaseStock(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Este método borra el producto del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el producto borrado
     */
    override suspend fun delete(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    /**
     * Este método busca un producto con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el producto que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}