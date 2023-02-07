package koin.repositories.producto

import kotlinx.coroutines.flow.Flow
import koin.models.producto.Producto
import java.util.*

interface IProductoRepository<ID> {
    suspend fun findAll(): Flow<Producto>
    suspend fun findById(id: ID): Producto?
    suspend fun findByUUID(id: UUID): Producto?
    suspend fun save(entity: Producto): Producto
    suspend fun delete(id: ID): Producto?
    suspend fun decreaseStock(id: ID): Producto?
}