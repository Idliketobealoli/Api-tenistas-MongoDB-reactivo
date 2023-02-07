package com.example.tennislabspringboot.repositories.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de maquinas cacheadas
 */
interface IMaquinaRepository<ID> {
    suspend fun findAll(): Flow<Maquina>
    suspend fun findById(id: ID): Maquina?
    suspend fun findByUUID(id: UUID): Maquina?
    suspend fun save(entity: Maquina): Maquina
    suspend fun delete(id: ID): Maquina?
    suspend fun setInactive(id: ID): Maquina?
}