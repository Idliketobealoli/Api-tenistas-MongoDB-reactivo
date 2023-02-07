package koin.client

import koin.common.Request
import koin.dto.maquina.EncordadoraDTOcreate
import koin.dto.pedido.PedidoDTOcreate
import koin.dto.producto.ProductoDTOcreate
import koin.dto.tarea.AdquisicionDTOcreate
import koin.dto.turno.TurnoDTOcreate
import koin.dto.user.UserDTOcreate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import koin.models.pedido.PedidoState
import koin.models.producto.TipoProducto
import koin.models.user.UserProfile
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.LocalDateTime
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Menús que se visualizan durante la ejecución de la app
 * para acceder a las distintas funcionalidades
 */
private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private val raqueta = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaac"),
    TipoProducto.RAQUETAS, "Marca Inicial",
    "Raqueta inicial", 40.0, 15
)

private val producto = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaabb"),
    TipoProducto.FUNDAS, "Marca Inicial",
    "Funda inicial", 16.5, 15
)
private val user = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaaa"),
    "Armando", "Perez", "123456789",
    "prueba@uwu.ita", "1234", UserProfile.CLIENT,
    true
)
private val worker = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaab"),
    "Trabajador", "SinSueldo", "987654321",
    "prueba2@gmail.com", "1111", UserProfile.WORKER,
    true
)
private val encordadora = EncordadoraDTOcreate(
    modelo = "EncordadoraFalsa",
    marca =  "MarcaFalsa",
    numeroSerie = "NumeroDeSerieFalso123456",
    activa = false,
    isManual = true,
    maxTension = 69.69,
    minTension = 6.9
)
private val adquis = AdquisicionDTOcreate(
    raqueta = raqueta,
    precio = 69.69,
    pedidoId = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    productoAdquirido = producto
)

fun menu(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 7) {
        println("""
        Select something to do: 
        
        1. Users
        2. Productos
        3. Maquinas
        4. Tareas
        5. Pedidos
        6. Turnos
        7. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }

    return when (userInput) {
        1 -> menuUsers(input, output, token)
        2 -> menuProductos(input, output, token)
        3 -> menuMaquinas(input, output, token)
        4 -> menuTareas(input, output, token)
        5 -> menuPedidos(input, output, token)
        6 -> menuTurnos(input, output, token)
        7 -> true
        else -> true
    }
}

fun menuTurnos(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 2) {
                println("""
                        filter by fecha de turno?:
                        
                        1. no
                        2. Yes
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> Request(token, 6_1, null, Request.Type.REQUEST)
                else -> {
                    val fecha = LocalDateTime.now().minusMonths(1L).toString()
                    Request(token, 6_1, fecha, Request.Type.REQUEST)
                }
            }
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 6_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val entity = json.encodeToString(
                TurnoDTOcreate(
                    worker = worker,
                    maquina = encordadora,
                    tarea1 = adquis,
                    tarea2 = null
                )
            )
            val request = Request(token, 6_3, entity, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 6_4, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 6_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun menuPedidos(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6 || userInput == 4) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 4) {
                println("""
                        filter by pedido state?:
                        
                        1. no
                        2. Yes (PROCESO)
                        3. Yes (TERMINADO)
                        4. Yes (RECIBIDO)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> Request(token, 5_1, null, Request.Type.REQUEST)
                2 -> Request(token, 5_1, PedidoState.PROCESO.name, Request.Type.REQUEST)
                3 -> Request(token, 5_1, PedidoState.TERMINADO.name, Request.Type.REQUEST)
                else -> Request(token, 5_1, PedidoState.RECIBIDO.name, Request.Type.REQUEST)
            }
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 5_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val entity = json.encodeToString(PedidoDTOcreate(user = user))
            val request = Request(token, 5_3, entity, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 5_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun menuTareas(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 3) {
                println("""
                        filter by completed tareas?:
                        
                        1. no
                        2. Yes (show uncompleted tasks)
                        3. Yes (show completed tasks)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> Request(token, 4_1, null, Request.Type.REQUEST)
                2 -> Request(token, 4_1, false.toString(), Request.Type.REQUEST)
                3 -> Request(token, 4_1, true.toString(), Request.Type.REQUEST)
                else -> Request(token, 4_1, null, Request.Type.REQUEST)
            }
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 4_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val entity = json.encodeToString(adquis)
            val request = Request(token, 4_3, entity, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 4_4, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 4_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun menuMaquinas(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            val request = Request(token, 3_1, null, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 3_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val entity = json.encodeToString(encordadora)
            val request = Request(token, 3_3, entity, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 3_4, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 3_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun menuProductos(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 2) {
                println("""
                        filter by available products?:
                        
                        1. no
                        2. Yes
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> Request(token, 2_1, null, Request.Type.REQUEST)
                2 -> Request(token, 2_1, "disponibles", Request.Type.REQUEST)
                else -> Request(token, 2_1, null, Request.Type.REQUEST)
            }
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 2_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val entity = json.encodeToString(
                ProductoDTOcreate(
                    tipo = TipoProducto.OVERGRIPS,
                    marca = "MarcaFalsa",
                    modelo = "ProductoFalso",
                    precio = 69.69,
                    stock = 69
                )
            )
            val request = Request(token, 2_3, entity, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 2_4, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 2_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun menuUsers(input: DataInputStream, output: DataOutputStream, token: String): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 3) {
                println("""
                        filter by active users?:
                        
                        1. no
                        2. Yes (show active users)
                        3. Yes (show inactive users)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> Request(token, 1_1, null, Request.Type.REQUEST)
                2 -> Request(token, 1_1, true.toString(), Request.Type.REQUEST)
                3 -> Request(token, 1_1, false.toString(), Request.Type.REQUEST)
                else -> Request(token, 1_1, null, Request.Type.REQUEST)
            }
            sendRequest(input, output, request)
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 1_2, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        3 -> {
            val user = json.encodeToString(
                UserDTOcreate(
                    nombre = "Perico",
                    apellido = "Palotes",
                    telefono = "999666333",
                    email = "pericoPalote@gmail.com",
                    password = "me gustan los periquitos"
                )
            )
            val request = Request(token, 1_3, user, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 1_4, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = Request(token, 1_5, i, Request.Type.REQUEST)
            sendRequest(input, output, request)
        }
        6 -> true
        else -> true
    }
}

fun sendRequest(input: DataInputStream, output: DataOutputStream, request: Request): Boolean {
    output.writeUTF(json.encodeToString(request))
    val responseJSON = input.readUTF()
    println(responseJSON)
    return false
}
