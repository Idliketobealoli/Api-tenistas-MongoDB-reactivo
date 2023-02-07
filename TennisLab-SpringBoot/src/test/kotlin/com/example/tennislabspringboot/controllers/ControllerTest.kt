package com.example.tennislabspringboot.controllers

import com.example.tennislabspringboot.db.*
import com.example.tennislabspringboot.dto.maquina.PersonalizadoraDTOvisualize
import com.example.tennislabspringboot.dto.pedido.PedidoDTOvisualize
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.dto.tarea.PersonalizacionDTOvisualize
import com.example.tennislabspringboot.dto.turno.TurnoDTOvisualize
import com.example.tennislabspringboot.dto.user.UserDTOvisualize
import com.example.tennislabspringboot.mappers.PedidoMapper
import com.example.tennislabspringboot.mappers.TareaMapper
import com.example.tennislabspringboot.mappers.TurnoMapper
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.models.maquina.Maquina
import com.example.tennislabspringboot.models.maquina.TipoMaquina
import com.example.tennislabspringboot.models.pedido.Pedido
import com.example.tennislabspringboot.models.pedido.PedidoState
import com.example.tennislabspringboot.models.producto.Producto
import com.example.tennislabspringboot.models.producto.TipoProducto
import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.models.tarea.TipoTarea
import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.models.user.UserProfile
import com.example.tennislabspringboot.repositories.maquina.MaquinaRepositoryCached
import com.example.tennislabspringboot.repositories.pedido.PedidoRepositoryCached
import com.example.tennislabspringboot.repositories.producto.ProductoRepositoryCached
import com.example.tennislabspringboot.repositories.tarea.TareaRepositoryCached
import com.example.tennislabspringboot.repositories.turno.TurnoRepositoryCached
import com.example.tennislabspringboot.repositories.user.UserRepositoryCached
import com.example.tennislabspringboot.services.login.create
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class ControllerTest {

    @MockK
    lateinit var repoM: MaquinaRepositoryCached

    @MockK
    lateinit var repoPedido: PedidoRepositoryCached

    @MockK
    lateinit var repoProd: ProductoRepositoryCached

    @MockK
    lateinit var repoTarea: TareaRepositoryCached

    @MockK
    lateinit var repoTurno: TurnoRepositoryCached

    @MockK
    lateinit var repoUser: UserRepositoryCached

    @MockK
    lateinit var tarMapper: TareaMapper

    @MockK
    lateinit var turMapper: TurnoMapper

    @MockK
    lateinit var pedMapper: PedidoMapper

    @InjectMockKs
    lateinit var controller: Controller

    private val users = getUsers()
    private val admin = users[0]
    private val adminToken = create(admin.fromDTO())
    private val userNormal = users[1]
    private val userToken = create(userNormal.fromDTO())
    private val productos = getProducts()
    private val pedidos = getPedidos()
    private val maquinas = getMaquinas()
    private val turnos = getTurnos()
    private val tareas = getTareas()

    private val raqueta = Producto(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    private val raquetaDTO = ProductoDTOvisualize(
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    private val client = User(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0006-48a7-b34f-05b596ea839a"),
        nombre = "Maria",
        apellido = "Martinez",
        telefono = "632120281",
        email = "email2@email.com",
        password = "contra",
        perfil = UserProfile.CLIENT,
        activo = true
    )
    private val clientDTO = UserDTOvisualize(
        nombre = "Maria",
        apellido = "Martinez",
        email = "email2@email.com",
        perfil = UserProfile.CLIENT,
        activo = true
    )
    private val worker = User(
        id= ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        nombre = "Luis",
        apellido = "Martinez",
        telefono = "632950281",
        email = "email@email.com",
        password = "estacontraseñanoestaensha512",
        perfil = UserProfile.WORKER,
        activo = true
    )
    private val workerDTO = UserDTOvisualize(
        nombre = "Luis",
        apellido = "Martinez",
        email = "email@email.com",
        perfil = UserProfile.WORKER,
        activo = true
    )
    private val producto = Producto(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0004-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
    )
    private val pedido = Pedido(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = client.uuid,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        precio = 0.0
    )
    private val tarea = Tarea(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0013-48a7-b34f-05b596ea83cc"),
        raquetaId = raqueta.uuid,
        precio = producto.precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = true,
        pedidoId = pedido.uuid,
        productoAdquiridoId = producto.uuid,
        peso = 10,
        balance = 2.0,
        rigidez = 5,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
    private val tareaDTO = PersonalizacionDTOvisualize(
        raqueta = raquetaDTO,
        precio = producto.precio,
        finalizada = true,
        pedidoId = pedido.uuid,
        peso = 10,
        balance = 2.0,
        rigidez = 5
    )
    private val pedidoDTO = PedidoDTOvisualize(
        user = clientDTO,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        tareas = listOf(tareaDTO),
        precio = 0.0
    )
    private val personalizadora1 = Maquina(
        id = ObjectId.get(),
        uuid = UUID.fromString("93a98d69-0008-48a7-b34f-05b596ea83bb"),
        modelo = "RTX-3080TI",
        marca = "Nvidia",
        fechaAdquisicion = LocalDate.parse("2022-11-10"),
        numeroSerie = "123456789X",
        tipo = TipoMaquina.PERSONALIZADORA,
        activa = true,
        isManual = null,
        maxTension = null,
        minTension = null,
        measuresRigidity = false,
        measuresBalance = true,
        measuresManeuverability = true
    )
    private val personalizadora1DTO = PersonalizadoraDTOvisualize(
        modelo = "RTX-3080TI",
        marca = "Nvidia",
        fechaAdquisicion = LocalDate.parse("2022-11-10"),
        numeroSerie = "123456789X",
        activa = true,
        measuresRigidity = false,
        measuresBalance = true,
        measuresManeuverability = true
    )
    private val turno = Turno(
        uuid = UUID.fromString("93a98d69-0019-48a7-b34f-05b596ea8abc"),
        workerId = worker.uuid,
        maquinaId = personalizadora1.uuid,
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = LocalDateTime.of(2002, 10, 14, 16, 49),
        numPedidosActivos = 2,
        tarea1Id = tarea.uuid,
        tarea2Id = null,
        finalizado = false
    )
    private val turnoDTO = TurnoDTOvisualize(
        worker = workerDTO,
        maquina = personalizadora1DTO,
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = LocalDateTime.of(2002, 10, 14, 16, 49),
        numPedidosActivos = 2,
        tarea1 = tareaDTO,
        tarea2 = null,
        finalizado = false
    )

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findMaquinaById() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "modelo" : "RTX-3080TI",
                "marca" : "Nvidia",
                "fechaAdquisicion" : [ 2022, 11, 10 ],
                "numeroSerie" : "123456789X",
                "activa" : true,
                "measuresManeuverability" : true,
                "measuresRigidity" : false,
                "measuresBalance" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery{ repoM.findByUUID(personalizadora1.uuid)} returns personalizadora1

        var result = ""
        launch { result = controller.findMaquinaById(personalizadora1.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun findMaquinaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val response = """
            {
              "headers" : { },
              "body" : "Maquina with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findMaquinaById(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }


    @Test
    fun findAllMaquinasSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "maquina" : [ {
                  "modelo" : "RTX-3080TI",
                  "marca" : "Nvidia",
                  "fechaAdquisicion" : [ 2022, 11, 10 ],
                  "numeroSerie" : "123456789X",
                  "activa" : true,
                  "measuresManeuverability" : true,
                  "measuresRigidity" : false,
                  "measuresBalance" : true
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoM.findAll() } returns flowOf(personalizadora1)

        var result = ""
        launch { result = controller.findAllMaquinas() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllMaquinasError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No maquinas found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoM.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllMaquinas() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createMaquinaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "modelo" : "RTX-3080TI",
                "marca" : "Nvidia",
                "fechaAdquisicion" : [ 2022, 11, 10 ],
                "numeroSerie" : "123456789X",
                "activa" : true,
                "measuresManeuverability" : true,
                "measuresRigidity" : false,
                "measuresBalance" : true
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { repoM.save(any()) } returns personalizadora1
        val ress = controller.createMaquina(maquinas[0], adminToken)
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createMaquinaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoM.save(any())} returns personalizadora1
        val ress = controller.createMaquina(maquinas[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createMaquinaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoM.save(any())} returns personalizadora1
        val ress = controller.createMaquina(maquinas[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteMaquinaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "modelo" : "RTX-3080TI",
                "marca" : "Nvidia",
                "fechaAdquisicion" : [ 2022, 11, 10 ],
                "numeroSerie" : "123456789X",
                "activa" : true,
                "measuresManeuverability" : true,
                "measuresRigidity" : false,
                "measuresBalance" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteMaquinaIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete Maquina with id 93a98d69-0008-48a7-b34f-05b596ea83bb.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns null
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteMaquinaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteMaquinaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "modelo" : "RTX-3080TI",
                "marca" : "Nvidia",
                "fechaAdquisicion" : [ 2022, 11, 10 ],
                "numeroSerie" : "123456789X",
                "activa" : true,
                "measuresManeuverability" : true,
                "measuresRigidity" : false,
                "measuresBalance" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot find and set inactive maquina with id 93a98d69-0008-48a7-b34f-05b596ea83bb.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns null
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //-----------------------------PEDIDOS-----------------------------------
    @Test
    fun findPedidoById() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery{ repoPedido.findByUUID(pedido.uuid)} returns pedido

        var result = ""
        launch { result = controller.findPedidoById(pedido.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun findPedidoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val response = """
            {
              "headers" : { },
              "body" : "Pedido with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findPedidoById(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }


    @Test
    fun findAllPedidosSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "pedidos" : [ {
                  "user" : {
                    "nombre" : "Maria",
                    "apellido" : "Martinez",
                    "email" : "email2@email.com",
                    "perfil" : "CLIENT",
                    "activo" : true
                  },
                  "state" : "PROCESO",
                  "fechaEntrada" : [ 2013, 10, 10 ],
                  "fechaSalida" : [ 2023, 12, 12 ],
                  "topeEntrega" : [ 2023, 12, 14 ],
                  "tareas" : [ {
                    "raqueta" : {
                      "tipo" : "RAQUETAS",
                      "marca" : "MarcaRaqueta",
                      "modelo" : "ModeloRaqueta",
                      "precio" : 150.5,
                      "stock" : 3
                    },
                    "precio" : 36.4,
                    "finalizada" : true,
                    "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                    "peso" : 10,
                    "balance" : 2.0,
                    "rigidez" : 5
                  } ],
                  "precio" : 0.0
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { pedMapper.toDTO(listOf(pedido)) } returns listOf(pedidoDTO)
        coEvery { repoPedido.findAll() } returns flowOf(pedido)

        var result = ""
        launch { result = controller.findAllPedidos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllPedidosError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No pedidos found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoPedido.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllPedidos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createPedidoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "user" : {
                  "nombre" : "Maria",
                  "apellido" : "Martinez",
                  "email" : "email2@email.com",
                  "perfil" : "CLIENT",
                  "activo" : true
                },
                "state" : "PROCESO",
                "fechaEntrada" : [ 2013, 10, 10 ],
                "fechaSalida" : [ 2023, 12, 12 ],
                "topeEntrega" : [ 2023, 12, 14 ],
                "tareas" : [ {
                  "raqueta" : {
                    "tipo" : "RAQUETAS",
                    "marca" : "MarcaRaqueta",
                    "modelo" : "ModeloRaqueta",
                    "precio" : 150.5,
                    "stock" : 3
                  },
                  "precio" : 36.4,
                  "finalizada" : true,
                  "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                  "peso" : 10,
                  "balance" : 2.0,
                  "rigidez" : 5
                } ],
                "precio" : 0.0
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { pedMapper.toDTO(pedido)} returns pedidoDTO
        coEvery { repoUser.findByUUID(any())} returns client
        coEvery { repoTarea.save(any()) } returns tarea
        coEvery { repoPedido.save(any()) } returns pedido
        val ress = controller.createPedido(pedidos[0], adminToken)
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createPedidoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoPedido.save(any())} returns pedido
        val ress = controller.createPedido(pedidos[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createPedidoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoPedido.save(any())} returns pedido
        val ress = controller.createPedido(pedidos[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deletePedidoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoTarea.findAll() } returns flowOf()
        coEvery { repoTarea.delete(any()) } returns tarea
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deletePedidoIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete pedido with id 93a98d69-0010-48a7-b34f-05b596ea8acc.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoTarea.findAll() } returns flowOf()
        coEvery { repoTarea.delete(any()) } returns tarea
        coEvery { repoPedido.delete(pedido.id) } returns null
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deletePedidoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deletePedidoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    //-----------------------------PRODUCTO-----------------------------------
    @Test
    fun findProductoById() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "tipo" : "FUNDAS",
                "marca" : "MarcaZ",
                "modelo" : "ModeloZ",
                "precio" : 36.4,
                "stock" : 8
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery{ repoProd.findByUUID(producto.uuid)} returns producto

        var result = ""
        launch { result = controller.findProductoById(producto.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun findProductoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val response = """
            {
              "headers" : { },
              "body" : "Producto with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findProductoById(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }


    @Test
    fun findAllProductosSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "productos" : [ {
                  "tipo" : "FUNDAS",
                  "marca" : "MarcaZ",
                  "modelo" : "ModeloZ",
                  "precio" : 36.4,
                  "stock" : 8
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoProd.findAll() } returns flowOf(producto)

        var result = ""
        launch { result = controller.findAllProductos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllProductosError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No productos found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoProd.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllProductos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createProductoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "tipo" : "FUNDAS",
                "marca" : "MarcaZ",
                "modelo" : "ModeloZ",
                "precio" : 36.4,
                "stock" : 8
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { repoProd.save(any()) } returns producto
        val ress = controller.createProducto(productos[0], adminToken)
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createProductoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoProd.save(any())} returns producto
        val ress = controller.createProducto(productos[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createProductoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoProd.save(any())} returns producto
        val ress = controller.createProducto(productos[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteProductoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "tipo" : "FUNDAS",
                "marca" : "MarcaZ",
                "modelo" : "ModeloZ",
                "precio" : 36.4,
                "stock" : 8
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteProductoIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete producto with id 93a98d69-0004-48a7-b34f-05b596ea83aa.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns null
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteProductoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteProductoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockProductoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "tipo" : "FUNDAS",
                "marca" : "MarcaZ",
                "modelo" : "ModeloZ",
                "precio" : 36.4,
                "stock" : 8
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Cannot decrease stock. Producto with id 93a98d69-0004-48a7-b34f-05b596ea83aa not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns null
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    //-------------------------TAREA----------------------------
    @Test
    fun findTareaById() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery{ repoTarea.findByUUID(tarea.uuid)} returns tarea

        var result = ""
        launch { result = controller.findTareaById(tarea.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun findTareaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val response = """
            {
              "headers" : { },
              "body" : "Tarea with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findTareaById(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }


    @Test
    fun findAllTareasSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "tareas" : [ {
                  "raqueta" : {
                    "tipo" : "RAQUETAS",
                    "marca" : "MarcaRaqueta",
                    "modelo" : "ModeloRaqueta",
                    "precio" : 150.5,
                    "stock" : 3
                  },
                  "precio" : 36.4,
                  "finalizada" : true,
                  "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                  "peso" : 10,
                  "balance" : 2.0,
                  "rigidez" : 5
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { tarMapper.toDTO(listOf(tarea)) } returns listOf(tareaDTO)
        coEvery { repoTarea.findAll() } returns flowOf(tarea)

        var result = ""
        launch { result = controller.findAllTareas() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllTareasError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No tareas found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoTarea.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllTareas() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTareaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "raqueta" : {
                  "tipo" : "RAQUETAS",
                  "marca" : "MarcaRaqueta",
                  "modelo" : "ModeloRaqueta",
                  "precio" : 150.5,
                  "stock" : 3
                },
                "precio" : 36.4,
                "finalizada" : true,
                "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                "peso" : 10,
                "balance" : 2.0,
                "rigidez" : 5
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { tarMapper.toDTO(tarea) } returns tareaDTO
        coEvery { repoTarea.save(any()) } returns tarea
        val ress = controller.createTarea(personalizacion, adminToken)
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTareaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTarea.save(any())} returns tarea
        val ress = controller.createTarea(tareas[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTareaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTarea.save(any())} returns tarea
        val ress = controller.createTarea(tareas[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteTareaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTareaIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete tarea with id 93a98d69-0013-48a7-b34f-05b596ea83cc.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns null
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTareaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTareaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot find and set finalizada tarea with id 93a98d69-0013-48a7-b34f-05b596ea83cc.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns null
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //------------------------------TURNO----------------------------------
    @Test
    fun findTurnoById() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery{ repoTurno.findByUUID(turno.uuid)} returns turno

        var result = ""
        launch { result = controller.findTurnoById(turno.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun findTurnoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val response = """
            {
              "headers" : { },
              "body" : "Turno with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findTurnoById(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }


    @Test
    fun findAllTurnosSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "turnos" : [ {
                  "worker" : {
                    "nombre" : "Luis",
                    "apellido" : "Martinez",
                    "email" : "email@email.com",
                    "perfil" : "WORKER",
                    "activo" : true
                  },
                  "maquina" : {
                    "modelo" : "RTX-3080TI",
                    "marca" : "Nvidia",
                    "fechaAdquisicion" : [ 2022, 11, 10 ],
                    "numeroSerie" : "123456789X",
                    "activa" : true,
                    "measuresManeuverability" : true,
                    "measuresRigidity" : false,
                    "measuresBalance" : true
                  },
                  "horaInicio" : [ 2002, 10, 14, 10, 9 ],
                  "horaFin" : [ 2002, 10, 14, 16, 49 ],
                  "numPedidosActivos" : 2,
                  "tarea1" : {
                    "raqueta" : {
                      "tipo" : "RAQUETAS",
                      "marca" : "MarcaRaqueta",
                      "modelo" : "ModeloRaqueta",
                      "precio" : 150.5,
                      "stock" : 3
                    },
                    "precio" : 36.4,
                    "finalizada" : true,
                    "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                    "peso" : 10,
                    "balance" : 2.0,
                    "rigidez" : 5
                  },
                  "tarea2" : null,
                  "finalizado" : false
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { turMapper.toDTO(listOf(turno)) } returns listOf(turnoDTO)
        coEvery { repoTurno.findAll() } returns flowOf(turno)

        var result = ""
        launch { result = controller.findAllTurnos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllTurnosError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No turnos found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoTurno.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllTurnos() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTurnoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "worker" : {
                  "nombre" : "Luis",
                  "apellido" : "Martinez",
                  "email" : "email@email.com",
                  "perfil" : "WORKER",
                  "activo" : true
                },
                "maquina" : {
                  "modelo" : "RTX-3080TI",
                  "marca" : "Nvidia",
                  "fechaAdquisicion" : [ 2022, 11, 10 ],
                  "numeroSerie" : "123456789X",
                  "activa" : true,
                  "measuresManeuverability" : true,
                  "measuresRigidity" : false,
                  "measuresBalance" : true
                },
                "horaInicio" : [ 2002, 10, 14, 10, 9 ],
                "horaFin" : [ 2002, 10, 14, 16, 49 ],
                "numPedidosActivos" : 2,
                "tarea1" : {
                  "raqueta" : {
                    "tipo" : "RAQUETAS",
                    "marca" : "MarcaRaqueta",
                    "modelo" : "ModeloRaqueta",
                    "precio" : 150.5,
                    "stock" : 3
                  },
                  "precio" : 36.4,
                  "finalizada" : true,
                  "pedidoId" : "93a98d69-0010-48a7-b34f-05b596ea8acc",
                  "peso" : 10,
                  "balance" : 2.0,
                  "rigidez" : 5
                },
                "tarea2" : null,
                "finalizado" : false
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { turMapper.fromDTO(turnos[0]) } returns turno
        coEvery { turMapper.toDTO(turno) } returns turnoDTO
        coEvery { repoTurno.save(any()) } returns turno
        val ress = controller.createTurno(turnos[0], adminToken)
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTurnoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTurno.save(any())} returns turno
        val ress = controller.createTurno(turnos[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTurnoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTurno.save(any())} returns turno
        val ress = controller.createTurno(turnos[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteTurnoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTurnoIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete Turno with id 93a98d69-0019-48a7-b34f-05b596ea8abc.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns null
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTurnoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTurnoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadoTurnoCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Invalid id.",
              "statusCode" : "BAD_REQUEST",
              "statusCodeValue" : 400
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot find and set finalizado turno with id 93a98d69-0019-48a7-b34f-05b596ea8abc.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns null
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //---------------------------------USER-------------------------------
    @Test
    fun findUserByUUID() = runTest {
        val ress = """
            {
              "headers" : { },
              "body" : {
                "nombre" : "Maria",
                "apellido" : "Martinez",
                "email" : "email2@email.com",
                "perfil" : "CLIENT",
                "activo" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery{ repoUser.findByUUID(client.uuid)} returns client

        var result = ""
        launch { result = controller.findUserByUuid(client.uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(ress, res) }
        )
    }

    @Test
    fun findUserNotExistsByUUID() = runTest {
        val uuid = UUID.randomUUID()
        val ress = """
            {
              "headers" : { },
              "body" : "User with id $uuid not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findUserByUuid(uuid.toString()) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(ress, res)}
        )
    }

    @Test
    fun findUserById() = runTest {
        val ress = """
            {
              "headers" : { },
              "body" : {
                "nombre" : "Maria",
                "apellido" : "Martinez",
                "email" : "email2@email.com",
                "perfil" : "CLIENT",
                "activo" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery{ repoUser.findById(1)} returns client

        var result = ""
        launch { result = controller.findUserById(1) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(ress, res) }
        )
    }

    @Test
    fun findUserNotExistsById() = runTest {
        val ress = """
            {
              "headers" : { },
              "body" : "User with id 1 not found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoUser.findById(1) } returns null

        var result = ""
        launch { result = controller.findUserById(1) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(ress, res)}
        )
    }

    @Test
    fun findAllUsersSuccess() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "users" : [ {
                  "nombre" : "Maria",
                  "apellido" : "Martinez",
                  "email" : "email2@email.com",
                  "perfil" : "CLIENT",
                  "activo" : true
                } ]
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoUser.findAll() } returns flowOf(client)

        var result = ""
        launch { result = controller.findAllUsers() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun findAllUsersError() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No users found.",
              "statusCode" : "NOT_FOUND",
              "statusCodeValue" : 404
            }
        """.trimIndent()
        coEvery { repoUser.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllUsers() }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createUserCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "nombre" : "Maria",
                "apellido" : "Martinez",
                "email" : "email2@email.com",
                "perfil" : "CLIENT",
                "activo" : true
              },
              "statusCode" : "CREATED",
              "statusCodeValue" : 201
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any()) } returns client
        val ress = controller.createUser(users[0], adminToken)
        val res = ress.replace("\r\n", "\n")
        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createUserIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any())} returns client
        val ress = controller.createUser(users[0], "")
        val res = ress.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createUserIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any())} returns client
        val ress = controller.createUser(users[0], userToken)
        val res = ress.replace("\r\n", "\n")

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteUserCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "nombre" : "Maria",
                "apellido" : "Martinez",
                "email" : "email2@email.com",
                "perfil" : "CLIENT",
                "activo" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteUserIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot delete user with id 93a98d69-0006-48a7-b34f-05b596ea839a.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns null
        var result = ""
        launch { result = controller.deleteUser(client.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteUserIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteUserIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveUserCorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : {
                "nombre" : "Maria",
                "apellido" : "Martinez",
                "email" : "email2@email.com",
                "perfil" : "CLIENT",
                "activo" : true
              },
              "statusCode" : "OK",
              "statusCodeValue" : 200
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveUserIncorrect() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "No token detected.",
              "statusCode" : "UNAUTHORIZED",
              "statusCodeValue" : 401
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid.toString(), "") }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveUserIncorrectToken() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "You are not allowed to to this.",
              "statusCode" : "FORBIDDEN",
              "statusCodeValue" : 403
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid.toString(), userToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveUserIncorrectNull() = runTest {
        val response = """
            {
              "headers" : { },
              "body" : "Unexpected error. Cannot find and set inactive user with id 93a98d69-0006-48a7-b34f-05b596ea839a.",
              "statusCode" : "INTERNAL_SERVER_ERROR",
              "statusCodeValue" : 500
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns null
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid.toString(), adminToken) }.join()
        val res = result.replace("\r\n", "\n")

        assertAll(
            { assertEquals(response, res) }
        )

    }
}