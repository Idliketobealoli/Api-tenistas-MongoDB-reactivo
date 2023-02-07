package koin.repositories.user

import kotlinx.coroutines.flow.Flow
import koin.models.user.User
import java.util.*

/**
 * Interfaz del repositorio de usuarios
 */
interface IUserRepository<ID> {
    suspend fun findAll(): Flow<User>
    suspend fun findById(id: ID): User?
    suspend fun findByUUID(id: UUID): User?
    suspend fun save(entity: User): User
    suspend fun delete(id: ID): User?
    suspend fun setInactive(id: ID): User?
}