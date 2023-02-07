package com.example.tennislabspringboot.services.login

import com.example.tennislabspringboot.dto.user.UserDTOLogin
import com.example.tennislabspringboot.dto.user.UserDTORegister
import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOvisualize
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.mappers.toDTO
import com.example.tennislabspringboot.repositories.user.UserRepositoryCached
import com.example.tennislabspringboot.services.utils.checkUserEmailAndPhone
import com.example.tennislabspringboot.services.utils.fieldsAreIncorrect
import com.example.tennislabspringboot.services.utils.matches

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