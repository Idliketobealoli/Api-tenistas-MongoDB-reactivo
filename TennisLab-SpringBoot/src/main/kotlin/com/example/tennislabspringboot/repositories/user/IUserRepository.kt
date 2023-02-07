package com.example.tennislabspringboot.repositories.user

import com.example.tennislabspringboot.models.user.User
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de usuarios cacheados
 */
interface IUserRepository<ID> {
    suspend fun findAll(): Flow<User>
    suspend fun findById(id: ID): User?
    suspend fun findById(id: Int): User?
    suspend fun findByUUID(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByPhone(phone: String): User?
    suspend fun save(entity: User): User
    suspend fun delete(id: ID): User?
    suspend fun setInactive(id: ID): User?
}