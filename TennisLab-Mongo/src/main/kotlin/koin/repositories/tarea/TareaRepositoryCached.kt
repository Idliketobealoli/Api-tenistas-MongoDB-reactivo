package koin.repositories.tarea

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import koin.mappers.fromAPItoTarea
import koin.mappers.toDTOapi
import koin.models.tarea.Tarea
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.tarea.ITareaCache
import koin.services.ktorfit.KtorFitClient
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de tareas cacheados
 * @constructor Hace uso de la interfaz del repositorio de tareas
 * no cacheados y de la interfaz de caché
 * @property client instancia del cliente de Ktorfit
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de tareas cacheados
 */
@Single
@Named("TareaRepositoryCached")
class TareaRepositoryCached(
    @Named("TareaRepository")
    private val repo: ITareaRepository<Id<Tarea>>,
    private val cache: ITareaCache
): ITareaRepository<Id<Tarea>> {
    private val client by lazy { KtorFitClient.instance }
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Tarea>()

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
     * Este método busca todos los tareas y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<Tarea> = withContext(Dispatchers.IO) {
        val findAllDB = repo.findAll().toList()
        val findAllApi = fromAPItoTarea(client.getAllTareas())
        val set = mutableSetOf<Tarea>()
        set.addAll(findAllDB)
        set.addAll(findAllApi)
        set.asFlow()
    }

    /**
     * Este método guarda la entidad pasada a listSearches y también en el repositorio
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        client.saveTareas(entity.toDTOapi())
        entity
    }

    /**
     * Este método busca un tarea que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return tarea con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de tareas y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): Tarea? = withContext(Dispatchers.IO) {
        var result: Tarea? = null

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
     * Este método cambia el estado del tarea con el identificador pasado a finalizada
     * y lo guarda en listSearches
     * @param id identificador de mongo
     * @return el tarea que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizada(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.setFinalizada(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Este método borra el tarea del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el tarea borrado
     */
    override suspend fun delete(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    /**
     * Este método busca un tarea con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el tarea que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}