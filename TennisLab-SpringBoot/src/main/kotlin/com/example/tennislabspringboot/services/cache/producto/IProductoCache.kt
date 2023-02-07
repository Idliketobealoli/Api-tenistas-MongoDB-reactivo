package com.example.tennislabspringboot.services.cache.producto

import com.example.tennislabspringboot.models.producto.Producto
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

/**
 * Interfaz para el caché de producto
 */
interface IProductoCache : ICache<UUID, Producto>