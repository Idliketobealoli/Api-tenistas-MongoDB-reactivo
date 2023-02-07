package com.example.tennislabspringboot.db

import com.example.tennislabspringboot.dto.maquina.EncordadoraDTOcreate
import com.example.tennislabspringboot.dto.maquina.PersonalizadoraDTOcreate
import com.example.tennislabspringboot.dto.pedido.PedidoDTOcreate
import com.example.tennislabspringboot.dto.producto.ProductoDTOcreate
import com.example.tennislabspringboot.dto.tarea.AdquisicionDTOcreate
import com.example.tennislabspringboot.dto.tarea.EncordadoDTOcreate
import com.example.tennislabspringboot.dto.tarea.PersonalizacionDTOcreate
import com.example.tennislabspringboot.dto.turno.TurnoDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.models.pedido.PedidoState
import com.example.tennislabspringboot.models.producto.TipoProducto
import com.example.tennislabspringboot.models.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

val u1 = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-aaaaaaaaaaaa"),
    "Daniel", "Rodriguez", "632855327",
    "loli@gmail.com", "quiero galletas", UserProfile.ADMIN,
    true
)
val u2 = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaaa"),
    "Armando", "Perez", "123456789",
    "prueba@uwu.ita", "1234", UserProfile.CLIENT,
    true
)
val u3 = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaab"),
    "Trabajador", "SinSueldo", "987654321",
    "prueba2@gmail.com", "1111", UserProfile.WORKER,
    true
)
val raqueta = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaac"),
    TipoProducto.RAQUETAS, "Marca Inicial",
    "Raqueta inicial", 40.0, 15
)
val cordaje1 = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaba"),
    TipoProducto.CORDAJES, "Marca Inicial",
    "Cordaje inicial", 20.5, 10
)
val prod1 = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaabb"),
    TipoProducto.FUNDAS, "Marca Inicial",
    "Funda inicial", 16.5, 15
)
val prod2 = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaabc"),
    TipoProducto.GRIPS, "Marca Inicial",
    "Grip inicial", 7.9, 8
)
val encordadora = EncordadoraDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaca"),
    "Encordadora Inicial", "Marca Inicial",
    LocalDate.now(), "123456789-XD", activa = true,
    isManual = false, 7.0, 2.5
)
val personalizadora = PersonalizadoraDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaacb"),
    "Personalizadora Inicial", "Marca Inicial",
    LocalDate.now(), "152637481-FF",
    measuresManeuverability = true, measuresRigidity = true,
    measuresBalance = true
)
val adquisicion = AdquisicionDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaacc"),
    raqueta, 15.0, finalizada = false,
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    prod1
)
val personalizacion = PersonalizacionDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabaa"),
    raqueta, finalizada = false,
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    10, 2.0, 5
)
val encordado = EncordadoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabab"),
    raqueta, finalizada = false,
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    6.0, cordaje1, 6.5,
    cordaje1, dosNudos = true
)
val pedido = PedidoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    u2, PedidoState.PROCESO, LocalDate.now(),
    LocalDate.now().plusDays(1L),
    LocalDate.now().plusWeeks(2L),
    getTareas()
)
val turno1 = TurnoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabba"),
    u3, encordadora, LocalDateTime.now().plusHours(2L),
    LocalDateTime.now().plusHours(4L),
    encordado, adquisicion, false
)
val turno2 = TurnoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabbb"),
    u3, personalizadora, LocalDateTime.now().plusDays(1L).plusHours(2L),
    LocalDateTime.now().plusDays(1L).plusHours(4L),
    personalizacion, null, false
)


fun getUsers() = listOf(u1, u2, u3)
fun getProducts() = listOf(raqueta, cordaje1, prod1, prod2)
fun getMaquinas() = listOf(encordadora, personalizadora)
fun getTareas() = listOf(adquisicion, encordado, personalizacion)
fun getPedidos() = listOf(pedido)
fun getTurnos() = listOf(turno1, turno2)