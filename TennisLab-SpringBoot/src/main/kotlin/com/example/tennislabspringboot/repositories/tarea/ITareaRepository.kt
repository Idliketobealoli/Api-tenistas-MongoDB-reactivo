package com.example.tennislabspringboot.repositories.tarea

import com.example.tennislabspringboot.models.tarea.Tarea
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de tareas cacheadas
 */
interface ITareaRepository<ID> {
    suspend fun findAll(): Flow<Tarea>
    suspend fun findById(id: ID): Tarea?
    suspend fun save(entity: Tarea): Tarea
    suspend fun delete(id: ID): Tarea?
    suspend fun setFinalizada(id: ID): Tarea?
    suspend fun findByUUID(id: UUID): Tarea?
}