package koin.repositories.maquina

import kotlinx.coroutines.flow.Flow
import koin.models.maquina.Maquina
import java.util.*

/**
 * Interfaz del repositorio de máquinas
 */
interface IMaquinaRepository<ID> {
    suspend fun findAll(): Flow<Maquina>
    suspend fun findById(id: ID): Maquina?
    suspend fun findByUUID(id: UUID): Maquina?
    suspend fun save(entity: Maquina): Maquina
    suspend fun delete(id: ID): Maquina?
    suspend fun setInactive(id: ID): Maquina?
}