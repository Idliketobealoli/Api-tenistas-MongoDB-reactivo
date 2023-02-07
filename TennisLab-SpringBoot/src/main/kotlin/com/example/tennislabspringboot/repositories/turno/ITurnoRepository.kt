package com.example.tennislabspringboot.repositories.turno

import com.example.tennislabspringboot.models.turno.Turno
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de turnos cacheados
 */
interface ITurnoRepository<ID> {
    suspend fun findAll(): Flow<Turno>
    suspend fun findById(id: ID): Turno?
    suspend fun save(entity: Turno): Turno
    suspend fun delete(id: ID): Turno?
    suspend fun setFinalizado(id: ID): Turno?
    suspend fun findByUUID(id: UUID): Turno?
}