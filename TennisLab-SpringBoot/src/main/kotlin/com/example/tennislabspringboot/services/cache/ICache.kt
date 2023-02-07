package com.example.tennislabspringboot.services.cache

import io.github.reactivecircus.cache4k.Cache

/**
 * @author Daniel Rodriguez Muñoz
 * Interfaz con los métodos para cachear
 */
interface ICache<ID : Any, T : Any> {
    val hasRefreshAllCacheJob: Boolean
    val refreshTime: Long
    val cache: Cache<ID, T>
}