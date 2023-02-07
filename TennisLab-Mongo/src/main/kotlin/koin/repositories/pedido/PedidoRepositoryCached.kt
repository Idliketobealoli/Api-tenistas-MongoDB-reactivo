package koin.repositories.pedido

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import koin.models.pedido.Pedido
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.pedido.IPedidoCache
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de pedidos cacheados
 * @constructor Hace uso de la interfaz del repositorio de pedidos
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de pedidos cacheados
 */
@Single
@Named("PedidoRepositoryCached")
class PedidoRepositoryCached(
    @Named("PedidoRepository")
    private val repo: IPedidoRepository<Id<Pedido>>,
    private val cache: IPedidoCache
): IPedidoRepository<Id<Pedido>> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Pedido>()

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
     * Este método busca todos los pedidos y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<Pedido> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    /**
     * Este método busca un pedido que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return pedido con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de pedidos y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): Pedido? = withContext(Dispatchers.IO) {
        var result: Pedido? = null

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
    override suspend fun save(entity: Pedido): Pedido = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    /**
     * Este método borra el pedido del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el pedido borrado
     */
    override suspend fun delete(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    /**
     * Este método busca un pedido con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el pedido que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}