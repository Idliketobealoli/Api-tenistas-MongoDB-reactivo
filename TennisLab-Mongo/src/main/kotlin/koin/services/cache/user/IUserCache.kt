package koin.services.cache.user

import koin.services.cache.ICache
import koin.models.user.User
import java.util.*

/**
 * Interfaz para el caché de usuarios
 */
interface IUserCache : ICache<UUID, User>