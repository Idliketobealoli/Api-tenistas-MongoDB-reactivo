package com.example.tennislabspringboot.repositories.user

import com.example.tennislabspringboot.models.user.User
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Interfaz del repositorio de productos
 */
@Repository
interface UserRepository : CoroutineCrudRepository<User, ObjectId> {
    fun findFirstByUuid(uuid: UUID) : Flow<User>
    fun findFirstByEmail(email: String) : Flow<User>
    fun findFirstByTelefono(telefono: String) : Flow<User>
}