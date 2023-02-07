package com.example.tennislabspringboot.repositories.turno

import com.example.tennislabspringboot.models.turno.Turno
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Interfaz del repositorio de turnos
 */
@Repository
interface TurnoRepository: CoroutineCrudRepository<Turno, ObjectId> {
    fun findFirstByUuid(uuid: UUID) : Flow<Turno>
}