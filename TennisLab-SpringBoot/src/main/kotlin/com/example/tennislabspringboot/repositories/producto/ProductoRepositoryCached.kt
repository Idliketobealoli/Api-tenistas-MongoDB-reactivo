package com.example.tennislabspringboot.repositories.producto

import com.example.tennislabspringboot.mappers.toDTO
import com.example.tennislabspringboot.models.producto.Producto
import com.example.tennislabspringboot.services.cache.producto.IProductoCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de productos cacheados
 * @constructor Hace uso de la interfaz del repositorio de productos
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de productos cacheados
 */
@Repository
class ProductoRepositoryCached
    @Autowired constructor(
    private val repo: ProductoRepository,
    private val cache: IProductoCache
): IProductoRepository<ObjectId> {
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
    override suspend fun findAllAsFlow() = flow {
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

        result = repo.findFirstByUuid(id).toList().firstOrNull()
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
    override suspend fun decreaseStock(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Producto(
            id = entity.id,
            uuid = entity.uuid,
            tipo = entity.tipo,
            marca = entity.marca,
            modelo = entity.modelo,
            precio = entity.precio,
            stock = entity.stock - 1
        )
        repo.save(result)
        listSearches.add(result)
        result
    }

    /**
     * Este método borra el producto del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el producto borrado
     */
    override suspend fun delete(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    /**
     * Este método busca un producto con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el producto que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Borra todos los productos
     */
    suspend fun deleteAll() {
        repo.deleteAll()
    }
}