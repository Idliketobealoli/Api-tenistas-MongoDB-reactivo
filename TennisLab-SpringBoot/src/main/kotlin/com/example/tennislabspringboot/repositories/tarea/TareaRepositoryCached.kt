package com.example.tennislabspringboot.repositories.tarea

import com.example.tennislabspringboot.dto.tarea.TareaDTOFromApi
import com.example.tennislabspringboot.mappers.fromAPItoTarea
import com.example.tennislabspringboot.mappers.toDTOapi
import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.services.cache.tarea.ITareaCache
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de tareas cacheados
 * @constructor Hace uso de la interfaz del repositorio de tareas
 * no cacheados y de la interfaz de caché
 * @property client instancia del cliente
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de tareas cacheados
 * @property apiUri cadena de texto del endpoint
 */
@Repository
class TareaRepositoryCached
    @Autowired constructor(
    private val repo: TareaRepository,
    private val cache: ITareaCache
): ITareaRepository<ObjectId> {
    private val apiUri = "https://jsonplaceholder.typicode.com/"
    private val client = RestTemplate()
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Tarea>()
    private val json = ObjectMapper()

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
        val findAllApi = fromAPItoTarea(client.getForObject("${apiUri}todos", Array<TareaDTOFromApi>::class.java)?.toList() ?: listOf())
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
        client.postForObject<TareaDTOFromApi>("${apiUri}todos", entity.toDTOapi(), TareaDTOFromApi::class)
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

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    /**
     * Este método cambia el estado del tarea con el identificador pasado a finalizada
     * y lo guarda en listSearches
     * @param id identificador de tipo ObjectId
     * @return el tarea que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setFinalizada(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Tarea(
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
        repo.save(result)
        listSearches.add(result)
        result
    }

    /**
     * Este método borra el tarea del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de tipo ObjectId
     * @return el tarea borrado
     */
    override suspend fun delete(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    /**
     * Este método busca un tarea con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de tipo ObjectId
     * @return el tarea que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Borra todas las tareas
     */
    suspend fun deleteAll() {
        repo.deleteAll()
    }
}