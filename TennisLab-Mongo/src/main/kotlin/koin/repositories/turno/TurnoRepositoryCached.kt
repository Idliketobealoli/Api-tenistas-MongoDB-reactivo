package koin.repositories.turno

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import koin.models.turno.Turno
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.turno.ITurnoCache
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de turnos cacheados
 * @constructor Hace uso de la interfaz del repositorio de turnos
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de turnos cacheados
 */
@Single
@Named("TurnoRepositoryCached")
class TurnoRepositoryCached(
    @Named("TurnoRepository")
    private val repo: ITurnoRepository<Id<Turno>>,
    private val cache: ITurnoCache
): ITurnoRepository<Id<Turno>> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Turno>()

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
     * Este método busca todos los turnos y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<Turno> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    /**
     * Este método guarda la entidad pasada a listSearches y también en el repositorio
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: Turno): Turno = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    /**
     * Este método busca un turno que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return turno con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de turnos y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): Turno? = withContext(Dispatchers.IO) {
        var result: Turno? = null

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
     * Este método cambia el estado del turno con el identificador pasado a finalizado
     * y lo guarda en listSearches
     * @param id identificador de mongo
     * @return el turno que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizado(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val result = repo.setFinalizado(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Este método borra el turno del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de mongo
     * @return el turno borrado
     */
    override suspend fun delete(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    /**
     * Este método busca un turno con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de mongo
     * @return el turno que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}