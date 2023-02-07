package org.koin.ksp.generated

import org.koin.dsl.*


public val koin_services_koin_KoinModule = module {
	single(qualifier=null) { koin.controllers.Controller(get(qualifier=org.koin.core.qualifier.StringQualifier("UserRepositoryCached")),get(qualifier=org.koin.core.qualifier.StringQualifier("TurnoRepositoryCached")),get(qualifier=org.koin.core.qualifier.StringQualifier("TareaRepositoryCached")),get(qualifier=org.koin.core.qualifier.StringQualifier("ProductoRepositoryCached")),get(qualifier=org.koin.core.qualifier.StringQualifier("PedidoRepositoryCached")),get(qualifier=org.koin.core.qualifier.StringQualifier("MaquinaRepositoryCached"))) } 
	single(qualifier=org.koin.core.qualifier.StringQualifier("MaquinaRepository")) { koin.repositories.maquina.MaquinaRepository() } bind(koin.repositories.maquina.IMaquinaRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("MaquinaRepositoryCached")) { koin.repositories.maquina.MaquinaRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("MaquinaRepository")),get()) } bind(koin.repositories.maquina.IMaquinaRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("PedidoRepository")) { koin.repositories.pedido.PedidoRepository() } bind(koin.repositories.pedido.IPedidoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("PedidoRepositoryCached")) { koin.repositories.pedido.PedidoRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("PedidoRepository")),get()) } bind(koin.repositories.pedido.IPedidoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("ProductoRepository")) { koin.repositories.producto.ProductoRepository() } bind(koin.repositories.producto.IProductoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("ProductoRepositoryCached")) { koin.repositories.producto.ProductoRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("ProductoRepository")),get()) } bind(koin.repositories.producto.IProductoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("TareaRepository")) { koin.repositories.tarea.TareaRepository() } bind(koin.repositories.tarea.ITareaRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("TareaRepositoryCached")) { koin.repositories.tarea.TareaRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("TareaRepository")),get()) } bind(koin.repositories.tarea.ITareaRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("TurnoRepository")) { koin.repositories.turno.TurnoRepository() } bind(koin.repositories.turno.ITurnoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("TurnoRepositoryCached")) { koin.repositories.turno.TurnoRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("TurnoRepository")),get()) } bind(koin.repositories.turno.ITurnoRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("UserRepository")) { koin.repositories.user.UserRepository() } bind(koin.repositories.user.IUserRepository::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("UserRepositoryCached")) { koin.repositories.user.UserRepositoryCached(get(qualifier=org.koin.core.qualifier.StringQualifier("UserRepository")),get()) } bind(koin.repositories.user.IUserRepository::class)
	single(qualifier=null) { koin.services.cache.maquina.MaquinaCache() } bind(koin.services.cache.maquina.IMaquinaCache::class)
	single(qualifier=null) { koin.services.cache.pedido.PedidoCache() } bind(koin.services.cache.pedido.IPedidoCache::class)
	single(qualifier=null) { koin.services.cache.producto.ProductoCache() } bind(koin.services.cache.producto.IProductoCache::class)
	single(qualifier=null) { koin.services.cache.tarea.TareaCache() } bind(koin.services.cache.tarea.ITareaCache::class)
	single(qualifier=null) { koin.services.cache.turno.TurnoCache() } bind(koin.services.cache.turno.ITurnoCache::class)
	single(qualifier=null) { koin.services.cache.user.UserCache() } bind(koin.services.cache.user.IUserCache::class)
}
public val koin.services.koin.KoinModule.module : org.koin.core.module.Module get() = koin_services_koin_KoinModule