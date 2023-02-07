package com.example.tennislabspringboot.repositories.turno

import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.services.cache.turno.ITurnoCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de turnos cacheados
 * @constructor Hace uso de la interfaz del repositorio de turnos
 * no cacheados y de la interfaz de caché
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de turnos cacheados
 */
@Repository
class TurnoRepositoryCached
    @Autowired constructor(
    private val repo: TurnoRepository,
    private val cache: ITurnoCache
): ITurnoRepository<ObjectId> {
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

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    /**
     * Este método cambia el estado del turno con el identificador pasado a finalizado
     * y lo guarda en listSearches
     * @param id identificador de tipo ObjectId
     * @return el turno que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizado(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Turno(
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
        repo.save(result)
        listSearches.add(result)
        result
    }

    /**
     * Este método borra el turno del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de tipo ObjectId
     * @return el turno borrado
     */
    override suspend fun delete(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    /**
     * Este método busca un turno con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de tipo ObjectId
     * @return el turno que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Borra todos los turnos
     */
    suspend fun deleteAll() {
        repo.deleteAll()
    }
}