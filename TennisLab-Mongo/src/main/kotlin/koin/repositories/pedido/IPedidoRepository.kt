package koin.repositories.pedido

import kotlinx.coroutines.flow.Flow
import koin.models.pedido.Pedido
import java.util.UUID

/**
 * Interfaz del repositorio de pedidos
 */
interface IPedidoRepository<ID> {
    suspend fun findAll(): Flow<Pedido>
    suspend fun findById(id: ID): Pedido?
    suspend fun findByUUID(id: UUID): Pedido?
    suspend fun save(entity: Pedido): Pedido
    suspend fun delete(id: ID): Pedido?
}