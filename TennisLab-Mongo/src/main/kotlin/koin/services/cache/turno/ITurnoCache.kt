package koin.services.cache.turno

import koin.services.cache.ICache
import koin.models.turno.Turno
import java.util.*

/**
 * Interfaz para el caché de turno
 */
interface ITurnoCache : ICache<UUID, Turno>