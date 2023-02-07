package koin.services.login

import koin.dto.user.UserDTOLogin
import koin.dto.user.UserDTORegister
import koin.dto.user.UserDTOcreate
import koin.dto.user.UserDTOvisualize
import koin.mappers.fromDTO
import koin.mappers.toDTO
import koin.repositories.user.UserRepositoryCached
import koin.services.utils.checkUserEmailAndPhone
import koin.services.utils.fieldsAreIncorrect
import koin.services.utils.matches

/**
 * @author Iván Azagra Troya
 * Archivo con las clases para el registro e inicio de sesión de usuarios
 */
suspend fun login(user: UserDTOLogin, repo: UserRepositoryCached): String? {
    val u = repo.findByEmail(user.email) ?: return null
    return if (!matches(user.password, u.password.encodeToByteArray())) null
    else create(u)
}

suspend fun register(user: UserDTORegister, repo: UserRepositoryCached): String? {
    val u = createUserWithoutToken(user.fromDTO(), repo)
    return if (u != null) {
        val res = repo.findByEmail(u.email)
        if (res == null) null
        else create(res)
    }
    else null
}

private suspend fun createUserWithoutToken(user: UserDTOcreate, repo: UserRepositoryCached): UserDTOvisualize? {
    if (fieldsAreIncorrect(user) || checkUserEmailAndPhone(user, repo))
        return null

    val res = repo.save(user.fromDTO())
    return res.toDTO()
}