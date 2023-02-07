package koin.models.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import koin.serializers.UUIDSerializer
import java.util.*

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de los usuarios.
 */
@Serializable
data class User(
    @BsonId @Contextual
    val id: Id<User> = newId(),
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile,
    val activo: Boolean
)