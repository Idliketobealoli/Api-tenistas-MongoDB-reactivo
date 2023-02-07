package koin.repositories.tarea

import kotlinx.coroutines.flow.Flow
import koin.models.tarea.Tarea
import java.util.*

/**
 * Interfaz del repositorio de tareas
 */
interface ITareaRepository<ID> {
    suspend fun findAll(): Flow<Tarea>
    suspend fun findById(id: ID): Tarea?
    suspend fun save(entity: Tarea): Tarea
    suspend fun delete(id: ID): Tarea?
    suspend fun setFinalizada(id: ID): Tarea?
    suspend fun findByUUID(id: UUID): Tarea?
}