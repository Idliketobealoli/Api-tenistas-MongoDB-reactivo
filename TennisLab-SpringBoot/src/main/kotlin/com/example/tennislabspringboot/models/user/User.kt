package com.example.tennislabspringboot.models.user

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de los usuarios.
 */
@Document
data class User(
    @Id
    val id: ObjectId = ObjectId.get(),
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile,
    val activo: Boolean
)