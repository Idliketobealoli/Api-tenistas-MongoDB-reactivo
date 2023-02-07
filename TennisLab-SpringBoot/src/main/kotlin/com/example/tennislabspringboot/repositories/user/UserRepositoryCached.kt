package com.example.tennislabspringboot.repositories.user

import com.example.tennislabspringboot.dto.user.UserDTOfromAPI
import com.example.tennislabspringboot.mappers.fromAPItoUser
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.services.cache.user.IUserCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Repositorio de usuarios cacheados
 * @constructor Hace uso de la interfaz del repositorio de Usuarios
 * no cacheados y de la interfaz de caché
 * @property client instancia del cliente
 * @property refreshJob encargado de realizar la tarea de refresco a través de una corrutina
 * @property listSearches lista mutable de usuarios cacheados
 * @property apiUri cadena de texto con la conexión al endpoint
 */
@Repository
class UserRepositoryCached
    @Autowired constructor(
    private val uRepo: UserRepository,
    private val cache: IUserCache
): IUserRepository<ObjectId> {
    private val apiUri = "https://jsonplaceholder.typicode.com/"
    private val client = RestTemplate()
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<User>()

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
     * Este método busca todos los usuarios y los guarda dentro del
     * set mutable en memoria que después se convierte en un flow
     */
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO) {
        val res = mutableSetOf<User>()
        res.addAll(uRepo.findAll().toList())
        res.addAll(fromAPItoUser(client.getForObject("${apiUri}users", Array<UserDTOfromAPI>::class.java)?.toList() ?: listOf()))
        res.asFlow()
    }

    /**
     * Este método busca un usuario que tenga el id pasado por parámetro que se encuentre en la caché
     * @param id identificador de tipo UUID
     * Convierte la caché en un mapa donde el identificador es la clave a buscar y si encuentra
     * un resultado lo añade a listSearches
     * @return usuario con el uuid pasado por parámetro, si no lo encuentra en el caché lo busca
     * en el repositorio de usuarios y lo añade a listSearches
     */
    override suspend fun findByUUID(id: UUID): User? = withContext(Dispatchers.IO) {
        var result: User? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = uRepo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    /**
     * Este método guarda la entidad pasada a listSearches y también en el repositorio
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        uRepo.save(entity)
        entity
    }

    /**
     * Este método cambia el estado del usuario con el identificador pasado de activo a
     * inactivo y lo guarda en listSearches
     * @param id identificador de tipo ObjectId
     * @return el usuario que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setInactive(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val entity = uRepo.findById(id) ?: return@withContext null

        val result = User(
            id = entity.id,
            uuid = entity.uuid,
            nombre = entity.nombre,
            apellido = entity.apellido,
            telefono = entity.telefono,
            email = entity.email,
            password = entity.password,
            perfil = entity.perfil,
            activo = false
        )
        uRepo.save(result)
        listSearches.add(result)
        result
    }

    /**
     * Este método borra el usuario del repositorio y de la caché en caso de que se encuentre
     * almacenado en ella
     * @param id identificador de tipo ObjectId
     * @return el usuario borrado
     */
    override suspend fun delete(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val entity = uRepo.findById(id) ?: return@withContext null
        uRepo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    /**
     * Este método busca un usuario con el identificador pasado por parámetro y lo guarda
     * en listSearches en la caché
     * @param id identificador de tipo ObjectId
     * @return el usuario que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val result = uRepo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    /**
     * Este método busca el usuario con el identificador pasado por parámetro y
     * lo guarda en la caché
     * @param id que recibimos desde el endpoint
     * @return el usuario que tiene ese identificador
     */
    override suspend fun findById(id: Int): User? = withContext(Dispatchers.IO) {
        try {
            val result = client.getForObject<UserDTOfromAPI>("${apiUri}users/$id", UserDTOfromAPI::class).fromDTO()
            listSearches.add(result)
            result
        }
        catch (e: Exception) {
            null
        }
    }

    /**
     * Busca el usuario que tenga el email introducido por parámetro entre todos los usuarios
     * guardados en la caché.
     * @param email del usuario a buscar
     * @return el usuario que contenga ese email
     */
    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        uRepo.findFirstByEmail(email).toList().firstOrNull()
        //findAll().toList().firstOrNull { it.email == email }
    }

    /**
     * Busca el usuario que tenga el teléfono introducido por parámetro entre todos los usuarios
     * guardados en la caché.
     * @param phone del usuario a buscar
     * @return el usuario que contenga ese teléfono
     */
    override suspend fun findByPhone(phone: String): User? = withContext(Dispatchers.IO) {
        uRepo.findFirstByTelefono(phone).toList().firstOrNull()
        //findAll().toList().firstOrNull { it.telefono == phone }
    }

    /**
     * Borra todos los usuarios
     */
    suspend fun deleteAll() {
        uRepo.deleteAll()
    }
}