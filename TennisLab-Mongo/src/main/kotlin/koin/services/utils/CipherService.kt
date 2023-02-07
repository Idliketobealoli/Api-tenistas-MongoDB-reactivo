package koin.services.utils

import com.toxicbakery.bcrypt.Bcrypt

/**
 * @author Iván Azagra Troya
 * Archivo con las funciones para cifrar y comprobar equivalencias usando Bcrypt
 */
fun cipher(message: String) : String {
    return Bcrypt.hash(message, 12).decodeToString()
}

fun matches(message: String, cipheredText: ByteArray) : Boolean {
    return Bcrypt.verify(message, cipheredText)
}