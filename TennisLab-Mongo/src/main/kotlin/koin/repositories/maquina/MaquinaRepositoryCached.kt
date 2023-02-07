package koin.repositories.maquina

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import koin.models.maquina.Maquina
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.maquina.IMaquinaCache
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de máquinas cacheados
 * @constructor Hace uso de la interfaz del repositorio de maquinas
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de maquinas cacheados
 */
@Single
@Named("MaquinaRepositoryCached")
class MaquinaRepositoryCached(
    @Named("MaquinaRepository")
    private val repo: IMaquinaRepository<Id<Maquina>>,
    private val cache: IMaquinaCache
): IMaquinaRepository<Id<Maquina>> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Maquina>()

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
     * Este método busca todos los maquinas y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<Maquina> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    /**
     * Este método busca un maquina que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return maquina con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de maquinas y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): Maquina? = withContext(Dispatchers.IO) {
        var result: Maquina? = null

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
    override suspend fun save(entity: Maquina): Maquina = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    /**
     * Este método cambia el estado del maquina con el identificador pasado de activo a
     * inactivo y lo guarda en listSearches
     * @param id identificador de mongo
     * @return el maquina que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setInactive(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val result = repo.setInactive(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Este método borra el maquina del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el maquina borrado
     */
    override suspend fun delete(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    /**
     * Este método busca un maquina con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el maquina que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}