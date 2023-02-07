package com.example.tennislabspringboot.repositories.pedido

import com.example.tennislabspringboot.models.pedido.Pedido
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de pedidos cacheados
 */
interface IPedidoRepository<ID> {
    suspend fun findAll(): Flow<Pedido>
    suspend fun findById(id: ID): Pedido?
    suspend fun findByUUID(id: UUID): Pedido?
    suspend fun save(entity: Pedido): Pedido
    suspend fun delete(id: ID): Pedido?
}