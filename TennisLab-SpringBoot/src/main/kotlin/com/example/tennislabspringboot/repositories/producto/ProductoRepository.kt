package com.example.tennislabspringboot.repositories.producto

import com.example.tennislabspringboot.models.producto.Producto
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Interfaz del repositorio de productos
 */
interface ProductoRepository: CoroutineCrudRepository<Producto, ObjectId> {
    fun findFirstByUuid(uuid: UUID) : Flow<Producto>
}