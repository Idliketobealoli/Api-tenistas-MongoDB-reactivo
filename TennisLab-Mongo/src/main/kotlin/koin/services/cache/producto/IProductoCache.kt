package koin.services.cache.producto

import koin.services.cache.ICache
import koin.models.producto.Producto
import java.util.*

/**
 * Interfaz para el caché de producto
 */
interface IProductoCache : ICache<UUID, Producto>