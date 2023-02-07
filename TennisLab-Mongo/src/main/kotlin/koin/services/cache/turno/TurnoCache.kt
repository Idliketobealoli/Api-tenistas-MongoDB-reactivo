package koin.services.cache.turno

import io.github.reactivecircus.cache4k.Cache
import koin.models.turno.Turno
import mu.KotlinLogging
import org.koin.core.annotation.Single
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {  }

/**
 * @author Daniel Rodriguez Muñoz
 * @property hasRefreshAllCacheJob boolean para saber si tiene refresco o no
 * @property refreshTime tiempo que tarda en refrescar
 * @property cache interfaz de la librería Cache4k con los atributos de nuestra caché
 */
@Single
class TurnoCache : ITurnoCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, Turno>()

    init { logger.debug { "Initializing TurnoCache..." } }
}