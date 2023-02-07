package koin.services.cache.maquina

import koin.services.cache.ICache
import koin.models.maquina.Maquina
import java.util.*

interface IMaquinaCache : ICache<UUID, Maquina>