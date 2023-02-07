package koin.dto.user

import kotlinx.serialization.Serializable
import koin.models.user.UserProfile
import koin.serializers.UUIDSerializer
import kotlinx.serialization.SerialName
import java.util.*

/**
 * @author Iván Azagra Troya
 * DTOs de creación, visualización y lista de visualización
 */
@Serializable
data class UserDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile = UserProfile.CLIENT,
    val activo: Boolean = true
)

@Serializable
@SerialName("UserDTOvisualize")
data class UserDTOvisualize(
    val nombre: String,
    val apellido: String,
    val email: String,
    val perfil: UserProfile,
    val activo: Boolean
)

@Serializable
@SerialName("UserDTOvisualizeList")
data class UserDTOvisualizeList(val users: List<UserDTOvisualize>)

@Serializable
data class UserDTOfromAPI(
    val name: String,
    val email: String,
    val phone: String
)

@Serializable
data class UserDTOLogin(
    val email: String,
    val password: String
)

@Serializable
data class UserDTORegister(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String
)