package koin.services.cache.pedido

import koin.services.cache.ICache
import koin.models.pedido.Pedido
import java.util.*

interface IPedidoCache : ICache<UUID, Pedido>