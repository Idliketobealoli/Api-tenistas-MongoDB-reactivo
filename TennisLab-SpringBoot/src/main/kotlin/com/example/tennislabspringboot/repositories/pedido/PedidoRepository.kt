package com.example.tennislabspringboot.repositories.pedido

import com.example.tennislabspringboot.models.pedido.Pedido
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Interfaz del repositorio de pedidos
 */
@Repository
interface PedidoRepository: CoroutineCrudRepository<Pedido, ObjectId> {
    fun findFirstByUuid(uuid: UUID): Flow<Pedido>
}