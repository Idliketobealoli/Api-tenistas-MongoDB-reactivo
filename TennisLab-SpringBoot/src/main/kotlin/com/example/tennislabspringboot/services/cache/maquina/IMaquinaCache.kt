package com.example.tennislabspringboot.services.cache.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

interface IMaquinaCache : ICache<UUID, Maquina>