package com.example.tennislabspringboot.mappers

import com.example.tennislabspringboot.dto.producto.ProductoDTOcreate
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.models.producto.Producto

/**
 * @author Iván Azagra Troya
 * Mapeador de Producto a DTO y de ProductoDTOCreate a Producto
 */
fun Producto.toDTO() =
    ProductoDTOvisualize (tipo, marca, modelo, precio, stock)

fun ProductoDTOcreate.fromDTO() = Producto(
    uuid = uuid,
    tipo = tipo,
    marca = marca,
    modelo = modelo,
    precio = precio,
    stock = stock
)

fun toDTO(list: List<Producto>) : List<ProductoDTOvisualize> {
    val res = mutableListOf<ProductoDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<ProductoDTOcreate>) : List<Producto> {
    val res = mutableListOf<Producto>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}