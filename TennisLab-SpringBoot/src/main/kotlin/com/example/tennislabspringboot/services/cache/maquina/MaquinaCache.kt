package com.example.tennislabspringboot.services.cache.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.minutes

/**
 * @author Iván Azagra Troya
 * @property hasRefreshAllCacheJob boolean para saber si tiene refresco o no
 * @property refreshTime tiempo que tarda en refrescar
 * @property cache interfaz de la librería Cache4k con los atributos de nuestra caché
 */
@Service
class MaquinaCache : IMaquinaCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, Maquina>()

    init { println("Initializing MaquinaCache...") }
}