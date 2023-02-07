package koin.services.ktorfit

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import koin.dto.tarea.TareaDTOFromApi
import koin.dto.user.UserDTOfromAPI

/**
 * @author Daniel Rodriguez Muñoz
 * Interfaz con los métodos necesarios para la consulta de datos mapeados a su correspondiente dirección
 */
interface IKtorFit {
    @GET("users")
    suspend fun getAll(): List<UserDTOfromAPI>

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Int): UserDTOfromAPI?

    @GET("todos")
    suspend fun getAllTareas(): List<TareaDTOFromApi>

    @POST("todos")
    suspend fun saveTareas(@Body tarea: TareaDTOFromApi): TareaDTOFromApi
}