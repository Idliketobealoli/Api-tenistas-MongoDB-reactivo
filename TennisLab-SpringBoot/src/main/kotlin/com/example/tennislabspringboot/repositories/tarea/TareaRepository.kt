package com.example.tennislabspringboot.repositories.tarea

import com.example.tennislabspringboot.models.tarea.Tarea
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Interfaz del repositorio de tareas
 */
interface TareaRepository: CoroutineCrudRepository<Tarea, ObjectId> {
    fun findFirstByUuid(uuid: UUID): Flow<Tarea>
}