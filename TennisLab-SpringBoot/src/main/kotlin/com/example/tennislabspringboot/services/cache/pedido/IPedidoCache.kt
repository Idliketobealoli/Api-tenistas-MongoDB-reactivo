package com.example.tennislabspringboot.services.cache.pedido

import com.example.tennislabspringboot.models.pedido.Pedido
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

interface IPedidoCache : ICache<UUID, Pedido>