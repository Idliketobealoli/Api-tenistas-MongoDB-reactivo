package koin.repositories.user

import koin.db.DBManager
import koin.models.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.user.*
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
 * Repositorio de usuarios
 */
@Single
@Named("UserRepository")
class UserRepository: IUserRepository<Id<User>> {
    /**
     * Este método busca todos los usuarios en la base de datos
     */
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO){
        logger.debug { "findAll()" }

        DBManager.database.getCollection<User>().find().publisher.asFlow()
    }

    /**
     * Este método busca un usuario que tenga el id pasado por parámetro
     * @param id identificador de tipo UUID
     * Recoge todos los datos de la base de datos y busca dentro de ella devolviendola como flow
     * y filtrando el usuario con el id.
     * @return usuario con el uuid pasado por parámetro
     */
    override suspend fun findByUUID(id: UUID): User? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<User>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
    }

    /**
     * Este método guarda la entidad pasada en la base de datos
     * @return la entidad pasada por parámetro
     */
    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<User>().save(entity).let {entity}
    }

    /**
     * Este método cambia el estado del usuario con el identificador pasado de activo a
     * inactivo
     * @param id identificador de mongo
     * @return el usuario que se haya puesto inactivo en caso de que exista
     */
    override suspend fun setInactive(id: Id<User>): User? = withContext(Dispatchers.IO) {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
            ?: return@withContext null
        val updated = User(
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
        DBManager.database.getCollection<User>().save(updated).let { updated }
    }

    /**
     * Este método borra el usuario del repositorio
     * @param id identificador de mongo
     * @return el usuario borrado
     */
    override suspend fun delete(id: Id<User>): User? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
        DBManager.database.getCollection<User>().deleteOneById(id).let { entity }
    }

    /**
     * Este método busca un usuario con el identificador pasado por parámetro
     * @param id identificador de mongo
     * @return el usuario que tenga el identificador pasado por parámetro
     */
    override suspend fun findById(id: Id<User>): User? = withContext(Dispatchers.IO){
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<User>().findOneById(id)
    }
}