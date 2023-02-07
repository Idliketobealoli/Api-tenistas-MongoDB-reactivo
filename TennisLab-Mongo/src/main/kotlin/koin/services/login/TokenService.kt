package koin.services.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import koin.models.ResponseError
import koin.models.user.User
import koin.models.user.UserProfile
import java.time.Instant
import java.util.*

private val algorithm: Algorithm = Algorithm.HMAC256("Elden Ring game of the year lets goooo")

/**
 * @author Daniel Rodriguez Muñoz
 * Contiene los métodos necesarios para la creación de tokens, comprobación y decodificación para verificarlos
 */
fun create(user: User): String {
    return JWT.create()
        .withClaim("id", user.uuid.toString())
        .withClaim("name", user.nombre)
        .withClaim("surname", user.apellido)
        .withClaim("email", user.email)
        .withClaim("profile", user.perfil.name)
        .withClaim("active", user.activo)
        .withExpiresAt(Date(System.currentTimeMillis() + (24*60*60*1_000)))
        .sign(algorithm)
}

fun decode(token: String): DecodedJWT? {
    val verifier = JWT.require(algorithm).build()

    return try {
        verifier.verify(token)
    } catch (_: Exception) {
        null
    }
}

fun checkToken(token: String, profile: UserProfile): String? {
    val decoded = decode(token)
        ?: return Json.encodeToString(ResponseError(401, "UNAUTHORIZED: No token detected"))
    if (decoded.getClaim("profile").isMissing || decoded.getClaim("active").isMissing ||
        decoded.getClaim("profile").isNull || decoded.getClaim("active").isNull ||
        decoded.getClaim("active").asBoolean() == false)
        return Json.encodeToString(ResponseError(401, "UNAUTHORIZED: Invalid token."))
    if (decoded.expiresAtAsInstant.isBefore(Instant.now()))
        return Json.encodeToString(ResponseError(401, "UNAUTHORIZED: Token expired."))
    when (profile) {
        UserProfile.ADMIN -> {
            if (!decoded.getClaim("profile").asString().equals(UserProfile.ADMIN.name)) {
                return Json.encodeToString(ResponseError(403, "FORBIDDEN: You are not allowed to to this."))
            }
        }
        UserProfile.WORKER -> {
            if (!(decoded.getClaim("profile").asString().equals(UserProfile.ADMIN.name) ||
                decoded.getClaim("profile").asString().equals(UserProfile.WORKER.name))) {
                return Json.encodeToString(ResponseError(403, "FORBIDDEN: You are not allowed to to this."))
            }
        }
        UserProfile.CLIENT -> {}
    }
    return null
}