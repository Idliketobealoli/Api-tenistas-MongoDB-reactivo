package koin.server

import koin.common.Request
import koin.controllers.Controller
import koin.db.*
import koin.dto.maquina.EncordadoraDTOvisualize
import koin.dto.maquina.MaquinaDTOcreate
import koin.dto.maquina.MaquinaDTOvisualizeList
import koin.dto.maquina.PersonalizadoraDTOvisualize
import koin.dto.pedido.PedidoDTOcreate
import koin.dto.pedido.PedidoDTOvisualize
import koin.dto.pedido.PedidoDTOvisualizeList
import koin.dto.producto.ProductoDTOcreate
import koin.dto.producto.ProductoDTOvisualize
import koin.dto.producto.ProductoDTOvisualizeList
import koin.dto.tarea.*
import koin.dto.turno.TurnoDTOcreate
import koin.dto.turno.TurnoDTOvisualize
import koin.dto.turno.TurnoDTOvisualizeList
import koin.dto.user.*
import koin.mappers.fromDTO
import koin.models.ResponseError
import koin.models.maquina.Maquina
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.producto.Producto
import koin.models.tarea.Tarea
import koin.models.turno.Turno
import koin.models.user.User
import koin.services.koin.KoinModule
import koin.services.login.create
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.util.*
import javax.net.ServerSocketFactory

/**
 * Puerto de conexión al servidor
 */
private const val PORT = 1708

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    useArrayPolymorphism = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(String::class, String.serializer())
            subclass(UserDTOvisualize::class)
            subclass(UserDTOvisualizeList::class)
            subclass(ProductoDTOvisualize::class)
            subclass(ProductoDTOvisualizeList::class)
            subclass(PedidoDTOvisualize::class)
            subclass(PedidoDTOvisualizeList::class)
            subclass(AdquisicionDTOvisualize::class)
            subclass(EncordadoDTOvisualize::class)
            subclass(PersonalizacionDTOvisualize::class)
            subclass(TareaDTOvisualizeList::class)
            subclass(EncordadoraDTOvisualize::class)
            subclass(PersonalizadoraDTOvisualize::class)
            subclass(MaquinaDTOvisualizeList::class)
            subclass(TurnoDTOvisualize::class)
            subclass(TurnoDTOvisualizeList::class)
            subclass(List::class, ListSerializer(PolymorphicSerializer(Any::class).nullable))
        }
    }
}

private lateinit var input: DataInputStream
private lateinit var output: DataOutputStream

/**
 * @author Daniel Rodriguez Muñoz
 * Función para crear y definir el comportamiento del servidor con el que trabaja la
 * aplicación
 */
fun main() = runBlocking {
    startKoin { modules(KoinModule().module) }
    val app = Application()
    loadData(app)
    var numClients = 0
    val serverFactory = ServerSocketFactory.getDefault() as ServerSocketFactory
    val serverSocket = serverFactory.createServerSocket(PORT) as ServerSocket

    val x = launch {
        app.controller.findAllProductosAsFlow()
            .onStart { println("Listening for changes in products...") }
            .distinctUntilChanged()
            .collect { println("Productos: ${json.encodeToString(it)}") }
    }

    while (true) {
        println("Awaiting clients...")
        val socket = serverSocket.accept()
        numClients++
        println("Attending client $numClients : ${socket.remoteSocketAddress}")

        coroutineScope { processClient(socket, app) }
    }
}

/**
 * Procesa la petición por parte de un usuario en la aplicación
 * para diferenciar la acción a realizar entre darse de alta,
 * iniciar sesión y procesar requerimiento
 */
suspend fun processClient(socket: Socket, app: Application) {
    input = DataInputStream(socket.inputStream)
    output = DataOutputStream(socket.outputStream)

    try{
        val requestJson = input.readUTF()
        val request = json.decodeFromString<Request>(requestJson)

        when(request.type) {
            Request.Type.LOGIN -> { sendLogin(output, request, app) }
            Request.Type.REGISTER -> { sendRegister(output, request, app) }
            Request.Type.REQUEST -> { processRequest(output, request, app) }
        }
    }
    catch (e: Exception) { println("Client disconnected, closing connection.") }
    finally {
        output.close()
        input.close()
        socket.close()
    }
}

/**
 * Procesa el requerimiento usando para ello la petición, el controlador y la salida de datos
 * @param output salida de datos de tipo DataOutputStream
 * @param request petición que recibe el servidor
 * @param app controlador de la aplicación
 */
fun processRequest(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    var response = ""
    if (request.code == null) response = json.encodeToString(ResponseError(400, "BAD REQUEST: no request code attached."))
    else {
        when (request.code) {
            1_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllUsers()
                else if (request.body.lowercase().toBooleanStrictOrNull() == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 1_1."))
                else app.controller.findAllUsersWithActivity(request.body.toBoolean())
            }
            1_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 1_2."))
                else if (request.body.toIntOrNull() != null)
                    app.controller.findUserById(request.body.toInt())
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findUserByUuid(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_2 is not an ID."))
                }
            }
            1_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_3."))
                else try {
                    val entity = json.decodeFromString<UserDTOcreate>(request.body)
                    app.controller.createUser(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_3 is not a UserDTOcreate."))
                }
            }
            1_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setInactiveUser(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_4 is not an ID."))
                }
            }
            1_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteUser(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_5 is not an ID."))
                }
            }

            2_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllProductos()
                else if (!request.body.lowercase().contentEquals("disponibles"))
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 2_1."))
                else app.controller.findAllProductosDisponibles()
            }
            2_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 2_2."))
                else try {
                    println(request.body)
                    val id = UUID.fromString(request.body)
                    app.controller.findProductoById(id)
                } catch (e: Exception) {
                    println(e)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_2 is not an ID."))
                }
            }
            2_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_3."))
                else try {
                    val entity = json.decodeFromString<ProductoDTOcreate>(request.body)
                    app.controller.createProducto(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_3 is not a ProductoDTOcreate."))
                }
            }
            2_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.decreaseStockFromProducto(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_4 is not an ID."))
                }
            }
            2_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteProducto(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_5 is not an ID."))
                }
            }

            3_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllMaquinas()
                else json.encodeToString(ResponseError(400, "BAD REQUEST: Request 1_1 must not have a body."))
            }
            3_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 3_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findMaquinaById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_2 is not an ID."))
                }
            }
            3_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_3."))
                else try {
                    val entity = json.decodeFromString<MaquinaDTOcreate>(request.body)
                    app.controller.createMaquina(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_3 is not a UserDTOcreate."))
                }
            }
            3_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setInactiveMaquina(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_4 is not an ID."))
                }
            }
            3_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteMaquina(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_5 is not an ID."))
                }
            }

            4_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllTareas()
                else if (request.body.lowercase().toBooleanStrictOrNull() == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 4_1."))
                else app.controller.findAllTareasFinalizadas(request.body.toBoolean())
            }
            4_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 4_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findTareaById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_2 is not an ID."))
                }
            }
            4_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_3."))
                else try {
                    val entity = json.decodeFromString<TareaDTOcreate>(request.body)
                    app.controller.createTarea(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_3 is not a UserDTOcreate."))
                }
            }
            4_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setFinalizadaTarea(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_4 is not an ID."))
                }
            }
            4_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteTarea(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_5 is not an ID."))
                }
            }

            5_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllPedidos()
                else if (!request.body.uppercase().trim().contentEquals(PedidoState.PROCESO.name) &&
                    !request.body.uppercase().trim().contentEquals(PedidoState.TERMINADO.name) &&
                    !request.body.uppercase().trim().contentEquals(PedidoState.RECIBIDO.name))
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 5_1."))
                else app.controller.findAllPedidosWithState(PedidoState.valueOf(request.body.uppercase().trim()))
            }
            5_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 5_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findPedidoById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_2 is not an ID."))
                }
            }
            5_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 5_3."))
                else try {
                    val entity = json.decodeFromString<PedidoDTOcreate>(request.body)
                    app.controller.createPedido(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_3 is not a UserDTOcreate."))
                }
            }
            5_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 5_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deletePedido(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_5 is not an ID."))
                }
            }

            6_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllTurnos()
                else try {
                    val fecha = LocalDateTime.parse(request.body)
                    app.controller.findAllTurnosByFecha(fecha)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_1 is not a LocalDateTime."))
                }
            }
            6_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 6_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findTurnoById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_2 is not an ID."))
                }
            }
            6_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_3."))
                else try {
                    val entity = json.decodeFromString<TurnoDTOcreate>(request.body)
                    app.controller.createTurno(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_3 is not a UserDTOcreate."))
                }
            }
            6_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setFinalizadoTurno(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_4 is not an ID."))
                }
            }
            6_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteTurno(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_5 is not an ID."))
                }
            }
        }
    }
    output.writeUTF(response)
}

/**
 * Envía la petición de inicio de sesión
 * @param output salida de datos de tipo DataOutputStream
 * @param request petición recibida
 * @param app controlador de la aplicación
 */
fun sendLogin(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    val response = if (request.body == null) json.encodeToString(ResponseError(400, "BAD REQUEST: no body attached."))
    else {
        try {
            val body = json.decodeFromString<UserDTOLogin>(request.body)
            app.controller.login(body)
        } catch (e: Exception) {
            json.encodeToString(ResponseError(400, "BAD REQUEST: Invalid body."))
        }
    }
    output.writeUTF(response)
}

/**
 * Envía la petición de registro de usuario
 * @param output salida de datos de tipo DataOutputStream
 * @param request petición recibida
 * @param app controlador de la aplicación
 */
suspend fun sendRegister(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    val response = if (request.body == null) json.encodeToString(ResponseError(400, "BAD REQUEST: no body attached."))
    else {
        try {
            val body = json.decodeFromString<UserDTORegister>(request.body)
            app.controller.register(body)
        } catch (e: Exception) {
            json.encodeToString(ResponseError(400, "BAD REQUEST: Invalid body."))
        }
    }
    output.writeUTF(response)
}

/**
 * Definición del controlador para la inyección por parte de Koin
 */
class Application : KoinComponent {
    val controller : Controller by inject()
}

/**
 * Carga de datos desde la base de datos a través del uso del controlador
 * @param app Controlador de la aplicación
 */
suspend fun loadData(app: Application) = runBlocking {
    DBManager.database.getCollection<User>().drop()
    DBManager.database.getCollection<Producto>().drop()
    DBManager.database.getCollection<Tarea>().drop()
    DBManager.database.getCollection<Pedido>().drop()
    DBManager.database.getCollection<Maquina>().drop()
    DBManager.database.getCollection<Turno>().drop()
    println("Loading data...")
    val users = getUsers()
    val admin = users[0]
    val adminToken = create(admin.fromDTO())

    val job1: Job = launch { users.forEach { println(app.controller.createUser(it, adminToken)) } }
    val job2: Job = launch { getProducts().forEach { println(app.controller.createProducto(it, adminToken)) } }
    val job3: Job = launch { getMaquinas().forEach { println(app.controller.createMaquina(it, adminToken)) } }
    joinAll(job1, job2, job3)
    val job4: Job = launch { getTareas().forEach { println(app.controller.createTarea(it, adminToken)) } }
    job4.join()
    val job5: Job = launch { getPedidos().forEach { println(app.controller.createPedido(it, adminToken)) } }
    val job6: Job = launch { getTurnos().forEach { println(app.controller.createTurno(it, adminToken)) } }
    joinAll(job5, job6)
    println("Data loaded.")
}