package com.example.tennislabspringboot.repositories.producto

import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.models.producto.Producto
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interfaz del repositorio de productos cacheados
 */
interface IProductoRepository<ID> {
    suspend fun findAll(): Flow<Producto>
    suspend fun findById(id: ID): Producto?
    suspend fun findByUUID(id: UUID): Producto?
    suspend fun save(entity: Producto): Producto
    suspend fun delete(id: ID): Producto?
    suspend fun decreaseStock(id: ID): Producto?
    suspend fun findAllAsFlow(): Flow<List<ProductoDTOvisualize>>
}