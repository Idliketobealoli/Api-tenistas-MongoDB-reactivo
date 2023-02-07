package com.example.tennislabspringboot.mappers

import com.example.tennislabspringboot.dto.user.UserDTORegister
import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOfromAPI
import com.example.tennislabspringboot.dto.user.UserDTOvisualize
import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.models.user.UserProfile
import com.example.tennislabspringboot.services.utils.cipher

/**
 * @author Iván Azagra Troya
 * Mapeador de User a DTOVisualize y de UserRegister a UserDTOCreate,
 * de UserDTOfromApi a UserDTOVisualize, de UserDTOfromApi a DTOVisualize
 * y de UserDTOfromApi a User
 */
fun User.toDTO() =
    UserDTOvisualize (nombre, apellido, email, perfil, activo)

fun UserDTOcreate.fromDTO() = User (
    uuid = uuid,
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email,
    password = cipher(password),
    perfil = perfil,
    activo = activo
)

fun UserDTORegister.fromDTO() = UserDTOcreate (
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email,
    password = password,
    perfil = UserProfile.CLIENT,
    activo = true
)

fun UserDTOfromAPI.fromDTO() = User(
    nombre = name.substringBeforeLast(" "),
    apellido = name.substringAfterLast(" "),
    telefono = phone,
    email = email,
    password = cipher("password fake"),
    perfil = UserProfile.CLIENT,
    activo = true
)

fun toDTO(list: List<User>) : List<UserDTOvisualize> {
    val res = mutableListOf<UserDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<UserDTOcreate>) : List<User> {
    val res = mutableListOf<User>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}

fun fromAPItoUser(list: List<UserDTOfromAPI>) : List<User> {
    val res = mutableListOf<User>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}