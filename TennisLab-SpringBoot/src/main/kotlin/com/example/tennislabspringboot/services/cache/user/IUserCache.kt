package com.example.tennislabspringboot.services.cache.user

import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

/**
 * Interfaz para el caché de usuarios
 */
interface IUserCache : ICache<UUID, User>