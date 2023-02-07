package com.example.tennislabspringboot.controllers

import com.example.tennislabspringboot.config.APIConfig
import com.example.tennislabspringboot.dto.maquina.MaquinaDTOcreate
import com.example.tennislabspringboot.dto.maquina.MaquinaDTOvisualizeList
import com.example.tennislabspringboot.dto.pedido.PedidoDTOcreate
import com.example.tennislabspringboot.dto.pedido.PedidoDTOvisualizeList
import com.example.tennislabspringboot.dto.producto.ProductoDTOcreate
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualizeList
import com.example.tennislabspringboot.dto.tarea.*
import com.example.tennislabspringboot.dto.turno.TurnoDTOcreate
import com.example.tennislabspringboot.dto.turno.TurnoDTOvisualizeList
import com.example.tennislabspringboot.dto.user.*
import com.example.tennislabspringboot.mappers.*
import com.example.tennislabspringboot.models.pedido.PedidoState
import com.example.tennislabspringboot.models.producto.TipoProducto
import com.example.tennislabspringboot.models.user.UserProfile
import com.example.tennislabspringboot.repositories.maquina.MaquinaRepositoryCached
import com.example.tennislabspringboot.repositories.pedido.PedidoRepositoryCached
import com.example.tennislabspringboot.repositories.producto.ProductoRepositoryCached
import com.example.tennislabspringboot.repositories.tarea.TareaRepositoryCached
import com.example.tennislabspringboot.repositories.turno.TurnoRepositoryCached
import com.example.tennislabspringboot.repositories.user.UserRepositoryCached
import com.example.tennislabspringboot.services.login.checkToken
import com.example.tennislabspringboot.services.utils.checkUserEmailAndPhone
import com.example.tennislabspringboot.services.utils.fieldsAreIncorrect
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que actúa como controlador de los distintos repositorios haciendo uso de los métodos requeridos y
 * devolviendo en cada caso dos tipos de respuesta: ResponseEntity y ResponseEntity por cada caso de los métodos
 */
@RestController
@RequestMapping(APIConfig.API_PATH)
class Controller
    @Autowired constructor(
        private val uRepo: UserRepositoryCached,
        private val turRepo: TurnoRepositoryCached,
        private val tarRepo: TareaRepositoryCached,
        private val proRepo: ProductoRepositoryCached,
        private val pedRepo: PedidoRepositoryCached,
        private val maRepo: MaquinaRepositoryCached,
        private val turMapper: TurnoMapper,
        private val tarMapper: TareaMapper,
        private val pedMapper: PedidoMapper,
) {
    private val json = ObjectMapper()
        .registerModule(JavaTimeModule())
        .writerWithDefaultPrettyPrinter()
    /**
     * @param id Identificador de tipo String
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista el usuario con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra un usuario con ese identificador
     */
    @GetMapping("/users/{id}")
    suspend fun findUserByUuid(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        if (id.toIntOrNull() != null) return@withContext findUserById(id.toInt())
        try {
            val user = uRepo.findByUUID(UUID.fromString(id))

            if (user == null) json.writeValueAsString(ResponseEntity("User with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(ResponseEntity(user.toDTO(), HttpStatus.OK))
        }
        catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * @param id Identificador de tipo int del objeto User
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return cadena de texto con los datos de cadena de texto con el error en caso de que no exista el usuario con ese identificador
     * @return cadena de texto con los datos de cadena de texto en formato JSON con el usuario y es estatus
     */
    suspend fun findUserById(id: Int) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        if (user == null) json.writeValueAsString(ResponseEntity("User with id $id not found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(ResponseEntity(user.toDTO(), HttpStatus.OK))
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan usuarios
     * @return cadena de texto con los datos de ResponseEntity con los datos de un UserDTOVisualizeList con la lista de usuarios
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllUsers() : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        if (users.isEmpty()) json.writeValueAsString(ResponseEntity("No users found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(ResponseEntity(UserDTOvisualizeList(toDTO(users)), HttpStatus.OK))
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren activos o inactivos dependiendo del parámetro pasado
     * @param active de tipo Boolean se usa para buscar a los usuarios que tengan el parametro "active" = true :? false
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan usuarios
     * @return cadena de texto con los datos de ResponseEntity con los datos de un UserDTOVisualizeList con la lista de usuarios
     *
     */
    @GetMapping("/users")
    suspend fun findAllUsersWithActivity(@RequestParam(required = false, name = "activo") active: String?) : String = withContext(Dispatchers.IO) {
        if (active == null) return@withContext findAllUsers()
        if (active.toBooleanStrictOrNull() == null)
            return@withContext json.writeValueAsString(ResponseEntity("Invalid request parameters.", HttpStatus.BAD_REQUEST))
        val activo = active.toBooleanStrictOrNull() ?: true
        val users = uRepo.findAll().toList().filter { it.activo == activo }

        if (users.isEmpty()) json.writeValueAsString(ResponseEntity("No users found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(ResponseEntity(UserDTOvisualizeList(toDTO(users)), HttpStatus.OK))
    }

    /**
     * Este método sirve para crear usuarios
     * @param user de tipo UserDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un usuario de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del UserDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que el usuario se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/users")
    suspend fun createUser(@RequestBody user: UserDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(user))
            return@withContext json.writeValueAsString(ResponseEntity("Cannot insert user. Incorrect fields.", HttpStatus.BAD_REQUEST))
        if (checkUserEmailAndPhone(user, uRepo))
            return@withContext json.writeValueAsString(ResponseEntity("Cannot insert user.", HttpStatus.BAD_REQUEST))

        val res = uRepo.save(user.fromDTO())
        json.writeValueAsString(ResponseEntity(res.toDTO(), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para establecer un usuario como inactivo
     * @param id de tipo UUID del usuario
     * @param token el token es una cadena de texto que se pasa el método por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no se encuentre el id o que no se pueda establecer como inactivo
     * @return cadena de texto con los datos de ResponseEntity con un enconde a String con formato json
     */
    @PutMapping("/users/{id}")
    suspend fun setInactiveUser(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val user = uRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot set inactive. User with id $id not found.", HttpStatus.NOT_FOUND))
            val result = uRepo.setInactive(user.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot find and set inactive user with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        }
        catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para borrar un usuario
     * @param id de tipo UUID del usuario que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el usuario no sea encontrado por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado al usuario encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/users/{id}")
    suspend fun deleteUser(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val user = uRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("NOT FOUND: Cannot delete. User with id $id not found.", HttpStatus.NOT_FOUND))
            val result = uRepo.delete(user.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete user with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * @param id Identificador de tipo UUID del objeto Pedido
     * Este método sirve para buscar un objeto de tipo Pedido con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista el pedido con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra un pedido con ese identificador
     */
    @GetMapping("/pedidos/{id}")
    suspend fun findPedidoById(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        try {
            val entity = pedRepo.findByUUID(UUID.fromString(id))

            if (entity == null) json.writeValueAsString(ResponseEntity("Pedido with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(ResponseEntity(pedMapper.toDTO(entity), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método devuelve todos los pedidos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan pedidos
     * @return cadena de texto con los datos de ResponseEntity con los datos de un PedidoDTOVisualizeList con la lista de pedidos
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllPedidos() : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(ResponseEntity("No pedidos found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(ResponseEntity(PedidoDTOvisualizeList(pedMapper.toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método devuelve todos los pedidos que con el estado requerido dependiendo del parámetro pasado
     * @param state el estado en el que se encuentra la lista de pedidos que devuelve el método
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan pedidos con ese estado
     * @return cadena de texto con los datos de ResponseEntity con los datos de un PedidoDTOVisualizeList con la lista de pedidos con el estado
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    @GetMapping("/pedidos")
    suspend fun findAllPedidosWithState(@RequestParam(required = false) state: String?) : String = withContext(Dispatchers.IO) {
        if (state == null) return@withContext findAllPedidos()
        try {
            val estado = PedidoState.valueOf(state)
            val entities = pedRepo.findAll().toList().filter { it.state == estado }

            if (entities.isEmpty()) json.writeValueAsString(
                ResponseEntity("No pedidos found with state = $state.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(
                ResponseEntity(PedidoDTOvisualizeList(pedMapper.toDTO(entities)), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(
                ResponseEntity("Invalid parameters.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para crear pedidos
     * @param entity de tipo PedidoDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del PedidoDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que el pedido se haya introducido de forma incorrecta o no haya sido encontrado el usuario
     * en caso de no dar error recoge las tareas del pedido  las guarda usando el repositorio de tareas y después guarda el pedido
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/pedidos")
    suspend fun createPedido(@RequestBody entity: PedidoDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert pedido. Incorrect fields.", HttpStatus.BAD_REQUEST))
        if (uRepo.findByUUID(entity.user.fromDTO().uuid) == null)
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert pedido. User not found.", HttpStatus.BAD_REQUEST))

        entity.tareas.forEach { tarRepo.save(it.fromDTO()) }
        val res = pedRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseEntity(pedMapper.toDTO(res), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para borrar un pedido
     * @param id de tipo UUID del pedido que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el pedido no sea encontrado por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado al pedido encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/pedidos/{id}")
    suspend fun deletePedido(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val uuid = UUID.fromString(id)
            val entity = pedRepo.findByUUID(uuid)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot delete. Pedido with id $id not found.", HttpStatus.NOT_FOUND))
            tarRepo.findAll().filter { it.pedidoId == uuid }.toList().forEach { tarRepo.delete(it.id) }
            val result = pedRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete pedido with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(pedMapper.toDTO(result), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * metodo para devolver los productos en tiempo real
     */
    suspend fun findAllProductosAsFlow() : Flow<List<ProductoDTOvisualize>> {
        return proRepo.findAllAsFlow()
    }

    /**
     * @param id Identificador de tipo UUID del objeto Producto
     * Este método sirve para buscar un objeto de tipo Producto con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista el producto con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra un producto con ese identificador
     */
    @GetMapping("/productos/{id}")
    suspend fun findProductoById(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        try {
            val entity = proRepo.findByUUID(UUID.fromString(id))

            if (entity == null) json.writeValueAsString(
                ResponseEntity("Producto with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(
                ResponseEntity(entity.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método devuelve todos los productos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan productos
     * @return cadena de texto con los datos de ResponseEntity con los datos de un ProductosDTOVisualizeList con la lista de productos
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    @GetMapping("/productos")
    suspend fun findAllProductos() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("No productos found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(ProductoDTOvisualizeList(toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método devuelve todos los productos que se encuentren disponibles
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan productos con ese estado
     * @return cadena de texto con los datos de ResponseEntity con los datos de un ProductosDTOVisualizeList con la lista de productos con el estado
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    @GetMapping("/productos/disponibles")
    suspend fun findAllProductosDisponibles() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList().filter { it.stock > 0 }

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("There are no products available.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(ProductoDTOvisualizeList(toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método sirve para crear productos
     * @param entity de tipo ProductosDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del ProductosDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que el producto se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/productos")
    suspend fun createProducto(@RequestBody entity: ProductoDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert producto. Incorrect fields.", HttpStatus.BAD_REQUEST))

        val res = proRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseEntity(res.toDTO(), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para borrar un producto
     * @param id de tipo UUID del pedido que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el producto no sea encontrado por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado al producto encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/productos/{id}")
    suspend fun deleteProducto(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = proRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot delete. Producto with id $id not found.", HttpStatus.NOT_FOUND))
            val result = proRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete producto with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para bajar el stock del producto con el id pasado por parámetro
     * @param id de tipo UUID del producto
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el producto no sea encontrado por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado al producto encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @PutMapping("/productos/decrease/{id}")
    suspend fun decreaseStockFromProducto(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = proRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot decrease stock. Producto with id $id not found.", HttpStatus.NOT_FOUND))
            val result = proRepo.decreaseStock(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot decrease stock. Producto with id $id not found.", HttpStatus.NOT_FOUND))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * @param id identificador de tipo UUID del objeto Máquina
     * Este método sirve para buscar un objeto de tipo Máquina con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista la máquina con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra una máquina con ese identificador
     * devuelve la respuesta en formato json
     */
    @GetMapping("/maquinas/{id}")
    suspend fun findMaquinaById(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        try {
            val entity = maRepo.findByUUID(UUID.fromString(id))

            if (entity == null) json.writeValueAsString(
                ResponseEntity("Maquina with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(
                ResponseEntity(entity.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.OK))
        }
    }

    /**
     * Este método devuelve todos las máquinas que se encuentren registradas en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan máquinas
     * @return cadena de texto con los datos de ResponseEntity con los datos de un MaquinaDTOVisualizeList con la lista de máquinas
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    @GetMapping("/maquinas")
    suspend fun findAllMaquinas() : String = withContext(Dispatchers.IO) {
        val entities = maRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("No maquinas found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(MaquinaDTOvisualizeList(toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método sirve para crear productos
     * @param entity de tipo MaquinaDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TurnoDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que la maquina se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/maquinas")
    suspend fun createMaquina(@RequestBody entity: MaquinaDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert maquina. Incorrect fields.", HttpStatus.BAD_REQUEST))

        val res = maRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseEntity(res.toDTO(), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para borrar un máquina
     * @param id de tipo UUID de la máquina que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que la máquina no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado a la máquina encontrada
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/maquinas/{id}")
    suspend fun deleteMaquina(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = maRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot delete. Maquina with id $id not found.", HttpStatus.NOT_FOUND))
            val result = maRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete Maquina with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }

    }

    /**
     * Este método sirve para poner en estado inactivo la máquina con el identificador pasado por parámetro
     * @param id de tipo UUID de la máquina
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que la máquina no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el cambio a inactivo de la máquina encontrada
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @PutMapping("/maquinas/{id}")
    suspend fun setInactiveMaquina(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = maRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot set inactive. Maquina with id $id not found.", HttpStatus.NOT_FOUND))
            val result = maRepo.setInactive(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot find and set inactive maquina with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(result.toDTO(), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * @param id identificador de tipo UUID del objeto Turno
     * Este método sirve para buscar un objeto de tipo Turno con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista el turno con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra un turno con ese identificador
     * devuelve la respuesta en formato json
     */
    @GetMapping("/turnos/{id}")
    suspend fun findTurnoById(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        try {
            val entity = turRepo.findByUUID(UUID.fromString(id))

            if (entity == null) json.writeValueAsString(
                ResponseEntity("Turno with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(ResponseEntity(turMapper.toDTO(entity), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método devuelve todos los turnos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan turnos
     * @return cadena de texto con los datos de ResponseEntity con los datos de un TurnoDTOVisualizeList con la lista de turnos
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllTurnos() : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("No turnos found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(TurnoDTOvisualizeList(turMapper.toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método sirve para buscar turnos por fecha y hora de inicio
     * @param horaInicio la fecha y hora del turno o turnos que se quieran buscar
     * Este método sirve para buscar un objeto de tipo Turno con la fecha pasada por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan turnos con esa fecha
     * @return cadena de texto con los datos de ResponseEntity si encuentra un turno con esa fecha
     * devuelve la respuesta en formato json
     */
    @GetMapping("/turnos")
    suspend fun findAllTurnosByFecha(@RequestParam(required = false) horaInicio: String?) : String = withContext(Dispatchers.IO) {
        if (horaInicio.isNullOrBlank()) return@withContext findAllTurnos()
        try {
            val hora = LocalDateTime.parse(horaInicio)
            val entities = turRepo.findAll().toList().filter { it.horaInicio == hora }

            if (entities.isEmpty()) json.writeValueAsString(
                ResponseEntity("No turnos found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(
                    ResponseEntity(TurnoDTOvisualizeList(turMapper.toDTO(entities)), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid parameters.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para crear turnos
     * @param entity de tipo TurnoDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TurnoDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que el turno se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/turnos")
    suspend fun createTurno(@RequestBody entity: TurnoDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.WORKER)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert turno. Incorrect fields.", HttpStatus.BAD_REQUEST))

        val res = turRepo.save(turMapper.fromDTO(entity))
        json.writeValueAsString(ResponseEntity(turMapper.toDTO(res), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para borrar un turno
     * @param id de tipo UUID del turno que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el turno no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado al turno encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/turnos/{id}")
    suspend fun deleteTurno(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = turRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot delete. Turno with id $id not found.", HttpStatus.NOT_FOUND))
            val result = turRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete Turno with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(turMapper.toDTO(result), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para poner en estado finalizado el turno con el identificador pasado por parámetro
     * @param id de tipo UUID del turno
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que el turno no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el cambio a inactivo del turno encontrado
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @PutMapping("/turnos/{id}")
    suspend fun setFinalizadoTurno(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = turRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot set finalizado. Turno with id $id not found.", HttpStatus.NOT_FOUND))
            val result = turRepo.setFinalizado(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot find and set finalizado turno with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(turMapper.toDTO(result), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * @param id identificador de tipo UUID del objeto Tarea
     * Este método sirve para buscar un objeto de tipo Tarea con el id pasado por parámetro
     * @return cadena de texto con los datos de ResponseEntity en caso de que no exista la tarea con ese identificador
     * @return cadena de texto con los datos de ResponseEntity si encuentra una tarea con ese identificador
     * devuelve la respuesta en formato json
     */
    @GetMapping("/tareas/{id}")
    suspend fun findTareaById(@PathVariable id: String) : String = withContext(Dispatchers.IO) {
        try {
            val entity = tarRepo.findByUUID(UUID.fromString(id))

            if (entity == null) json.writeValueAsString(
                ResponseEntity("Tarea with id $id not found.", HttpStatus.NOT_FOUND))
            else json.writeValueAsString(
                ResponseEntity(tarMapper.toDTO(entity), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método devuelve todos las tareas que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan tareas
     * @return cadena de texto con los datos de ResponseEntity con los datos de un TareaDTOVisualizeList con la lista de tareas
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllTareas() : String = withContext(Dispatchers.IO) {
        var entities = tarRepo.findAll().toList()
        if (entities.size > 25) entities = entities.subList(0,24)

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("No tareas found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(TareaDTOvisualizeList(tarMapper.toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método sirve para buscar tareas finalizadas
     * @param finalizada de tipo Boolean para definir el estado de las tareas a buscar
     * @return cadena de texto con los datos de ResponseEntity en caso de que no existan tareas con ese estado
     * @return cadena de texto con los datos de ResponseEntity si encuentra una tarea con ese estado true/false
     * devuelve la respuesta en formato json
     */
    @GetMapping("/tareas")
    suspend fun findAllTareasFinalizadas(@RequestParam(required = false) finalizada: String?) : String = withContext(Dispatchers.IO) {
        if (finalizada.isNullOrBlank()) return@withContext findAllTareas()
        if (finalizada.toBooleanStrictOrNull() == null)
            return@withContext json.writeValueAsString(
                ResponseEntity("Invalid parameters.", HttpStatus.BAD_REQUEST))
        val fin = finalizada.toBooleanStrictOrNull() ?: false
        var entities = tarRepo.findAll().toList().filter { it.finalizada == fin }
        if (entities.size > 25) entities = entities.subList(0,24)

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseEntity("No tareas found.", HttpStatus.NOT_FOUND))
        else json.writeValueAsString(
            ResponseEntity(TareaDTOvisualizeList(tarMapper.toDTO(entities)), HttpStatus.OK))
    }

    /**
     * Este método sirve para crear una Tarea
     * @param entity de tipo TareaDTOCreate
     * @param token para validar el método
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TareaDTOCreate y devuelve
     * @return cadena de texto con los datos de ResponseEntity en formato json en caso de que la tarea se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de ResponseEntity en caso de que no se hayan pasado datos suficientes para el cordaje
     * @return cadena de texto con los datos de ResponseEntity en caso de que haya una incoherencia de tipos en el EncordadoDTOCreate
     * @return cadena de texto con los datos de ResponseEntity en caso de que el parámetro no sea del tipo requerido en AdquisicionDTOCreate
     * @return cadena de texto con los datos de ResponseEntity en caso de que el parámetro no sea del tipo requerido en PersonalizacionDTOCreate
     * @return cadena de texto con los datos de ResponseEntity si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    @PostMapping("/tareas")
    suspend fun createTarea(@RequestBody entity: TareaDTOcreate, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert tarea. Incorrect fields.", HttpStatus.BAD_REQUEST))
        if (entity is EncordadoDTOcreate
            && (
            (entity.cordajeHorizontal.uuid == entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 2) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 1) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeVertical.stock < 1)
            ))
            return@withContext json.writeValueAsString(
                ResponseEntity("Cannot insert tarea. Not enough material for cordaje.", HttpStatus.BAD_REQUEST))

        when (entity) {
            is EncordadoDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS ||
                    entity.cordajeHorizontal.tipo != TipoProducto.CORDAJES ||
                    entity.cordajeVertical.tipo != TipoProducto.CORDAJES)
                    return@withContext json.writeValueAsString(
                        ResponseEntity("Cannot insert tarea. Type mismatch in product types.", HttpStatus.BAD_REQUEST))
            }
            is AdquisicionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.writeValueAsString(
                        ResponseEntity("Cannot insert tarea. Parameter is not of type Raqueta.", HttpStatus.BAD_REQUEST))
            }
            is PersonalizacionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.writeValueAsString(
                        ResponseEntity("Cannot insert tarea. Parameter is not of type Raqueta.", HttpStatus.BAD_REQUEST))
            }
        }

        val res = tarRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseEntity(tarMapper.toDTO(res), HttpStatus.CREATED))
    }

    /**
     * Este método sirve para borrar una tarea
     * @param id de tipo UUID del turno que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que la tarea no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el borrado a la tarea encontrada
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @DeleteMapping("/tareas/{id}")
    suspend fun deleteTarea(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = tarRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot delete. Tarea with id $id not found.", HttpStatus.NOT_FOUND))
            val result = tarRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot delete tarea with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(tarMapper.toDTO(result), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.BAD_REQUEST))
        }
    }

    /**
     * Este método sirve para poner en estado finalizado la tarea con el identificador pasado por parámetro
     * @param id de tipo UUID del turno
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el ResponseEntity correspondiente
     * @return cadena de texto con los datos de ResponseEntity en json del mensaje de error en caso de que la tarea no sea encontrada por el repositorio
     * @return cadena de texto con los datos de ResponseEntity en json en caso de que no se pueda aplicar el cambio a finalizada de la tarea encontrada
     * @return cadena de texto con los datos de ResponseEntity con formato json
     */
    @PutMapping("/tareas/{id}")
    suspend fun setFinalizadaTarea(@PathVariable id: String, @RequestHeader token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        try {
            val entity = tarRepo.findByUUID(UUID.fromString(id))
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Cannot set finalizado. Tarea with id $id not found.", HttpStatus.NOT_FOUND))
            val result = tarRepo.setFinalizada(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseEntity("Unexpected error. Cannot find and set finalizada tarea with id $id.", HttpStatus.INTERNAL_SERVER_ERROR))
            json.writeValueAsString(ResponseEntity(tarMapper.toDTO(result), HttpStatus.OK))
        } catch (e: Exception) {
            json.writeValueAsString(ResponseEntity("Invalid id.", HttpStatus.OK))
        }
    }

    /**
     * Este método sirve para iniciar sesión de un usuario
     * @param user de tipo UserDTOLogin
     * @return ResponseEntity en caso de que el token sea null
     * @return ResponseEntity si el token no es null
     */
    @GetMapping("/login")
    suspend fun login(@RequestBody user: UserDTOLogin): ResponseEntity<out String> = withContext(Dispatchers.IO) {
        val token = com.example.tennislabspringboot.services.login.login(user, uRepo)
        if (token == null) ResponseEntity("Unable to login. Incorrect email or password.", HttpStatus.NOT_FOUND)
        else ResponseEntity(token, HttpStatus.OK)
    }

    /**
     * Este método sirve para registrar un usuario
     * @param user de tipo UserDTORegister
     * @return ResponseEntity en caso de que el token sea null
     * @return ResponseEntity si el token no es null
     */
    @GetMapping("/register")
    suspend fun register(@RequestBody user: UserDTORegister): ResponseEntity<out String> = withContext(Dispatchers.IO) {
        val token = com.example.tennislabspringboot.services.login.register(user, uRepo)
        if (token == null) ResponseEntity("Unable to register. Incorrect parameters.", HttpStatus.BAD_REQUEST)
        else ResponseEntity(token, HttpStatus.OK)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        val j1 = launch { uRepo.deleteAll() }
        val j2 = launch { maRepo.deleteAll() }
        val j3 = launch { proRepo.deleteAll() }
        val j4 = launch { pedRepo.deleteAll() }
        val j5 = launch { turRepo.deleteAll() }
        val j6 = launch { tarRepo.deleteAll() }
        joinAll(j1, j2, j3, j4, j5, j6)
    }
}