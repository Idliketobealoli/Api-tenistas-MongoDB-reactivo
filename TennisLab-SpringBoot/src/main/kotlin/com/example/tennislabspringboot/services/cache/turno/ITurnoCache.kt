package com.example.tennislabspringboot.services.cache.turno

import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

/**
 * Interfaz para el caché de turno
 */
interface ITurnoCache : ICache<UUID, Turno>