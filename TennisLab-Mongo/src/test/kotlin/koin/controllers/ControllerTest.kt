package koin.controllers

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import koin.db.*
import koin.mappers.fromDTO
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.models.turno.Turno
import koin.models.user.User
import koin.models.user.UserProfile
import koin.repositories.maquina.MaquinaRepositoryCached
import koin.repositories.pedido.PedidoRepositoryCached
import koin.repositories.producto.ProductoRepositoryCached
import koin.repositories.tarea.TareaRepositoryCached
import koin.repositories.turno.TurnoRepositoryCached
import koin.repositories.user.UserRepositoryCached
import koin.services.login.create
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.newId
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

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
        id = newId(),
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    private val client = User(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0006-48a7-b34f-05b596ea839a"),
        nombre = "Maria",
        apellido = "Martinez",
        telefono = "632120281",
        email = "email2@email.com",
        password = "contra",
        perfil = UserProfile.CLIENT,
        activo = true
    )
    private val worker = User(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        nombre = "Luis",
        apellido = "Martinez",
        telefono = "632950281",
        email = "email@email.com",
        password = "estacontraseñanoestaensha512",
        perfil = UserProfile.WORKER,
        activo = true
    )
    private val producto = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0004-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
    )
    private val pedido = Pedido(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = client.uuid,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        precio = 0.0
    )
    private val tarea = Tarea(
        id = newId(),
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
    private val personalizadora1 = Maquina(
        id = newId(),
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

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findMaquinaById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["PersonalizadoraDTOvisualize", {
                            "modelo": "RTX-3080TI",
                            "marca": "Nvidia",
                            "fechaAdquisicion": "2022-11-10",
                            "numeroSerie": "123456789X",
                            "activa": true,
                            "measuresManeuverability": true,
                            "measuresRigidity": false,
                            "measuresBalance": true
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoM.findByUUID(personalizadora1.uuid)} returns personalizadora1

        var result = ""
        launch { result = controller.findMaquinaById(personalizadora1.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findMaquinaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: Maquina with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoM.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findMaquinaById(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllMaquinasSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["MaquinaDTOvisualizeList", {
                            "maquina": [
                                ["PersonalizadoraDTOvisualize", {
                                        "modelo": "RTX-3080TI",
                                        "marca": "Nvidia",
                                        "fechaAdquisicion": "2022-11-10",
                                        "numeroSerie": "123456789X",
                                        "activa": true,
                                        "measuresManeuverability": true,
                                        "measuresRigidity": false,
                                        "measuresBalance": true
                                    }
                                ]
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoM.findAll() } returns flowOf(personalizadora1)

        var result = ""
        launch { result = controller.findAllMaquinas() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllMaquinasError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No maquinas found."
                }
            ]
        """.trimIndent()
        coEvery { repoM.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllMaquinas() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createMaquinaCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": ["PersonalizadoraDTOvisualize", {
                        "modelo": "RTX-3080TI",
                        "marca": "Nvidia",
                        "fechaAdquisicion": "2022-11-10",
                        "numeroSerie": "123456789X",
                        "activa": true,
                        "measuresManeuverability": true,
                        "measuresRigidity": false,
                        "measuresBalance": true
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoM.save(any()) } returns personalizadora1
        val res = controller.createMaquina(maquinas[0], adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createMaquinaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoM.save(any())} returns personalizadora1
        val res = controller.createMaquina(maquinas[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createMaquinaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoM.save(any())} returns personalizadora1
        val res = controller.createMaquina(maquinas[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteMaquinaCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": ["PersonalizadoraDTOvisualize", {
                        "modelo": "RTX-3080TI",
                        "marca": "Nvidia",
                        "fechaAdquisicion": "2022-11-10",
                        "numeroSerie": "123456789X",
                        "activa": true,
                        "measuresManeuverability": true,
                        "measuresRigidity": false,
                        "measuresBalance": true
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteMaquinaIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete Maquina with id 93a98d69-0008-48a7-b34f-05b596ea83bb."
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns null
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteMaquinaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteMaquinaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.delete(personalizadora1.id) } returns personalizadora1
        var result = ""
        launch { result = controller.deleteMaquina(personalizadora1.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveMaquinaCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": ["PersonalizadoraDTOvisualize", {
                        "modelo": "RTX-3080TI",
                        "marca": "Nvidia",
                        "fechaAdquisicion": "2022-11-10",
                        "numeroSerie": "123456789X",
                        "activa": true,
                        "measuresManeuverability": true,
                        "measuresRigidity": false,
                        "measuresBalance": true
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns personalizadora1
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive maquina with id 93a98d69-0008-48a7-b34f-05b596ea83bb."
            }
        """.trimIndent()
        coEvery { repoM.findByUUID(any()) } returns personalizadora1
        coEvery { repoM.setInactive(personalizadora1.id)} returns null
        var result = ""
        launch { result = controller.setInactiveMaquina(personalizadora1.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }
    //-----------------------------PEDIDOS-----------------------------------
    @Test
    fun findPedidoById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["PedidoDTOvisualize", {
                            "user": null,
                            "state": "PROCESO",
                            "fechaEntrada": "2013-10-10",
                            "fechaSalida": "2023-12-12",
                            "topeEntrega": "2023-12-14",
                            "tareas": [
                            ],
                            "precio": 0.0
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoPedido.findByUUID(pedido.uuid)} returns pedido

        var result = ""
        launch { result = controller.findPedidoById(pedido.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findPedidoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: Pedido with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoPedido.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findPedidoById(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllPedidosSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["PedidoDTOvisualizeList", {
                            "pedidos": [
                                {
                                    "user": null,
                                    "state": "PROCESO",
                                    "fechaEntrada": "2013-10-10",
                                    "fechaSalida": "2023-12-12",
                                    "topeEntrega": "2023-12-14",
                                    "tareas": [
                                    ],
                                    "precio": 0.0
                                }
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoPedido.findAll() } returns flowOf(pedido)

        var result = ""
        launch { result = controller.findAllPedidos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllPedidosError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No pedidos found."
                }
            ]
        """.trimIndent()
        coEvery { repoPedido.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllPedidos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createPedidoCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": {
                    "user": null,
                    "state": "PROCESO",
                    "fechaEntrada": "2013-10-10",
                    "fechaSalida": "2023-12-12",
                    "topeEntrega": "2023-12-14",
                    "tareas": [
                    ],
                    "precio": 0.0
                }
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any())} returns client
        coEvery { repoTarea.save(any()) } returns tarea
        coEvery { repoPedido.save(any()) } returns pedido
        val res = controller.createPedido(pedidos[0], adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createPedidoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoPedido.save(any())} returns pedido
        val res = controller.createPedido(pedidos[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createPedidoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoPedido.save(any())} returns pedido
        val res = controller.createPedido(pedidos[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deletePedidoCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "user": null,
                    "state": "PROCESO",
                    "fechaEntrada": "2013-10-10",
                    "fechaSalida": "2023-12-12",
                    "topeEntrega": "2023-12-14",
                    "tareas": [
                    ],
                    "precio": 0.0
                }
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoTarea.findAll() } returns flowOf()
        coEvery { repoTarea.delete(any()) } returns tarea
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deletePedidoIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete pedido with id 93a98d69-0010-48a7-b34f-05b596ea8acc."
            }
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoTarea.findAll() } returns flowOf()
        coEvery { repoTarea.delete(any()) } returns tarea
        coEvery { repoPedido.delete(pedido.id) } returns null
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deletePedidoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deletePedidoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoPedido.findByUUID(any()) } returns pedido
        coEvery { repoPedido.delete(pedido.id) } returns pedido
        var result = ""
        launch { result = controller.deletePedido(pedido.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    //-----------------------------PRODUCTO-----------------------------------
    @Test
    fun findProductoById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["ProductoDTOvisualize", {
                            "tipo": "FUNDAS",
                            "marca": "MarcaZ",
                            "modelo": "ModeloZ",
                            "precio": 36.4,
                            "stock": 8
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoProd.findByUUID(producto.uuid)} returns producto

        var result = ""
        launch { result = controller.findProductoById(producto.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findProductoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: Producto with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoProd.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findProductoById(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllProductosSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["ProductoDTOvisualizeList", {
                            "productos": [
                                {
                                    "tipo": "FUNDAS",
                                    "marca": "MarcaZ",
                                    "modelo": "ModeloZ",
                                    "precio": 36.4,
                                    "stock": 8
                                }
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoProd.findAll() } returns flowOf(producto)

        var result = ""
        launch { result = controller.findAllProductos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllProductosError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No productos found."
                }
            ]
        """.trimIndent()
        coEvery { repoProd.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllProductos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createProductoCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": {
                    "tipo": "FUNDAS",
                    "marca": "MarcaZ",
                    "modelo": "ModeloZ",
                    "precio": 36.4,
                    "stock": 8
                }
            }
        """.trimIndent()
        coEvery { repoProd.save(any()) } returns producto
        val res = controller.createProducto(productos[0], adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createProductoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoProd.save(any())} returns producto
        val res = controller.createProducto(productos[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createProductoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoProd.save(any())} returns producto
        val res = controller.createProducto(productos[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteProductoCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "tipo": "FUNDAS",
                    "marca": "MarcaZ",
                    "modelo": "ModeloZ",
                    "precio": 36.4,
                    "stock": 8
                }
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteProductoIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete producto with id 93a98d69-0004-48a7-b34f-05b596ea83aa."
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns null
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteProductoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteProductoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.delete(producto.id) } returns producto
        var result = ""
        launch { result = controller.deleteProducto(producto.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun decreaseStockProductoCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "tipo": "FUNDAS",
                    "marca": "MarcaZ",
                    "modelo": "ModeloZ",
                    "precio": 36.4,
                    "stock": 8
                }
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns producto
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun decreaseStockProductoIncorrectNull() = runTest {
        val response = """
            {
                "code": 404,
                "message": "NOT FOUND: Cannot decrease stock. Producto with id 93a98d69-0004-48a7-b34f-05b596ea83aa not found."
            }
        """.trimIndent()
        coEvery { repoProd.findByUUID(any()) } returns producto
        coEvery { repoProd.decreaseStock(any())} returns null
        var result = ""
        launch { result = controller.decreaseStockFromProducto(producto.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    //-------------------------TAREA----------------------------
    @Test
    fun findTareaById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["PersonalizacionDTOvisualize", {
                            "raqueta": null,
                            "precio": 36.4,
                            "finalizada": true,
                            "pedidoId": "93a98d69-0010-48a7-b34f-05b596ea8acc",
                            "peso": 10,
                            "balance": 2.0,
                            "rigidez": 5
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoTarea.findByUUID(tarea.uuid)} returns tarea

        var result = ""
        launch { result = controller.findTareaById(tarea.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findTareaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: Tarea with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoTarea.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findTareaById(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllTareasSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["TareaDTOvisualizeList", {
                            "tareas": [
                                ["PersonalizacionDTOvisualize", {
                                        "raqueta": null,
                                        "precio": 36.4,
                                        "finalizada": true,
                                        "pedidoId": "93a98d69-0010-48a7-b34f-05b596ea8acc",
                                        "peso": 10,
                                        "balance": 2.0,
                                        "rigidez": 5
                                    }
                                ]
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoTarea.findAll() } returns flowOf(tarea)

        var result = ""
        launch { result = controller.findAllTareas() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTareasError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No tareas found."
                }
            ]
        """.trimIndent()
        coEvery { repoTarea.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllTareas() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createTareaCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": ["PersonalizacionDTOvisualize", {
                        "raqueta": null,
                        "precio": 36.4,
                        "finalizada": true,
                        "pedidoId": "93a98d69-0010-48a7-b34f-05b596ea8acc",
                        "peso": 10,
                        "balance": 2.0,
                        "rigidez": 5
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoTarea.save(any()) } returns tarea
        val res = controller.createTarea(personalizacion, adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTareaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTarea.save(any())} returns tarea
        val res = controller.createTarea(tareas[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTareaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTarea.save(any())} returns tarea
        val res = controller.createTarea(tareas[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteTareaCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": ["PersonalizacionDTOvisualize", {
                        "raqueta": null,
                        "precio": 36.4,
                        "finalizada": true,
                        "pedidoId": "93a98d69-0010-48a7-b34f-05b596ea8acc",
                        "peso": 10,
                        "balance": 2.0,
                        "rigidez": 5
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTareaIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete tarea with id ${tarea.uuid}."
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns null
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTareaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTareaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.delete(tarea.id) } returns tarea
        var result = ""
        launch { result = controller.deleteTarea(tarea.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadaTareaCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": ["PersonalizacionDTOvisualize", {
                        "raqueta": null,
                        "precio": 36.4,
                        "finalizada": true,
                        "pedidoId": "93a98d69-0010-48a7-b34f-05b596ea8acc",
                        "peso": 10,
                        "balance": 2.0,
                        "rigidez": 5
                    }
                ]
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns tarea
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizada tarea with id 93a98d69-0013-48a7-b34f-05b596ea83cc."
            }
        """.trimIndent()
        coEvery { repoTarea.findByUUID(any()) } returns tarea
        coEvery { repoTarea.setFinalizada(tarea.id)} returns null
        var result = ""
        launch { result = controller.setFinalizadaTarea(tarea.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }
    //------------------------------TURNO----------------------------------
    @Test
    fun findTurnoById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["TurnoDTOvisualize", {
                            "worker": null,
                            "maquina": null,
                            "horaInicio": "2002-10-14T10:09",
                            "horaFin": "2002-10-14T16:49",
                            "numPedidosActivos": 2,
                            "tarea1": null,
                            "tarea2": null,
                            "finalizado": false
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoTurno.findByUUID(turno.uuid)} returns turno

        var result = ""
        launch { result = controller.findTurnoById(turno.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findTurnoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: Turno with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoTurno.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findTurnoById(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllTurnosSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["TurnoDTOvisualizeList", {
                            "turnos": [
                                {
                                    "worker": null,
                                    "maquina": null,
                                    "horaInicio": "2002-10-14T10:09",
                                    "horaFin": "2002-10-14T16:49",
                                    "numPedidosActivos": 2,
                                    "tarea1": null,
                                    "tarea2": null,
                                    "finalizado": false
                                }
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoTurno.findAll() } returns flowOf(turno)

        var result = ""
        launch { result = controller.findAllTurnos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTurnosError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No turnos found."
                }
            ]
        """.trimIndent()
        coEvery { repoTurno.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllTurnos() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createTurnoCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": {
                    "worker": null,
                    "maquina": null,
                    "horaInicio": "2002-10-14T10:09",
                    "horaFin": "2002-10-14T16:49",
                    "numPedidosActivos": 2,
                    "tarea1": null,
                    "tarea2": null,
                    "finalizado": false
                }
            }
        """.trimIndent()
        coEvery { repoTurno.save(any()) } returns turno
        val res = controller.createTurno(turnos[0], adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTurnoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTurno.save(any())} returns turno
        val res = controller.createTurno(turnos[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createTurnoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTurno.save(any())} returns turno
        val res = controller.createTurno(turnos[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteTurnoCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "worker": null,
                    "maquina": null,
                    "horaInicio": "2002-10-14T10:09",
                    "horaFin": "2002-10-14T16:49",
                    "numPedidosActivos": 2,
                    "tarea1": null,
                    "tarea2": null,
                    "finalizado": false
                }
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTurnoIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete Turno with id ${turno.uuid}."
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns null
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTurnoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteTurnoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.delete(turno.id) } returns turno
        var result = ""
        launch { result = controller.deleteTurno(turno.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadoTurnoCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "worker": null,
                    "maquina": null,
                    "horaInicio": "2002-10-14T10:09",
                    "horaFin": "2002-10-14T16:49",
                    "numPedidosActivos": 2,
                    "tarea1": null,
                    "tarea2": null,
                    "finalizado": false
                }
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns turno
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setFinalizadoTurnoIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizado turno with id 93a98d69-0019-48a7-b34f-05b596ea8abc."
            }
        """.trimIndent()
        coEvery { repoTurno.findByUUID(any()) } returns turno
        coEvery { repoTurno.setFinalizado(turno.id)} returns null
        var result = ""
        launch { result = controller.setFinalizadoTurno(turno.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }
    //---------------------------------USER-------------------------------
    @Test
    fun findUserByUUID() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["UserDTOvisualize", {
                            "nombre": "Maria",
                            "apellido": "Martinez",
                            "email": "email2@email.com",
                            "perfil": "CLIENT",
                            "activo": true
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoUser.findByUUID(client.uuid)} returns client

        var result = ""
        launch { result = controller.findUserByUuid(client.uuid) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findUserNotExistsByUUID() = runTest {
        val uuid = UUID.randomUUID()
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: User with id $uuid not found."
                }
            ]
        """.trimIndent()
        coEvery { repoUser.findByUUID(uuid) } returns null

        var result = ""
        launch { result = controller.findUserByUuid(uuid) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }

    @Test
    fun findUserById() = runTest {
        val res = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["UserDTOvisualize", {
                            "nombre": "Maria",
                            "apellido": "Martinez",
                            "email": "email2@email.com",
                            "perfil": "CLIENT",
                            "activo": true
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery{ repoUser.findById(1)} returns client

        var result = ""
        launch { result = controller.findUserById(1) }.join()

        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findUserNotExistsById() = runTest {
        val res = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: User with id 1 not found."
                }
            ]
        """.trimIndent()
        coEvery { repoUser.findById(1) } returns null

        var result = ""
        launch { result = controller.findUserById(1) }.join()

        assertAll(
            { assertEquals(res, result)}
        )
    }

    @Test
    fun findAllUsersSuccess() = runTest {
        val response = """
            ["ResponseSuccess", {
                    "code": 200,
                    "data": ["UserDTOvisualizeList", {
                            "users": [
                                {
                                    "nombre": "Maria",
                                    "apellido": "Martinez",
                                    "email": "email2@email.com",
                                    "perfil": "CLIENT",
                                    "activo": true
                                }
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()
        coEvery { repoUser.findAll() } returns flowOf(client)

        var result = ""
        launch { result = controller.findAllUsers() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllUsersError() = runTest {
        val response = """
            ["ResponseError", {
                    "code": 404,
                    "message": "NOT FOUND: No users found."
                }
            ]
        """.trimIndent()
        coEvery { repoUser.findAll() } returns flowOf()

        var result = ""
        launch { result = controller.findAllUsers() }.join()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createUserCorrect() = runTest {
        val response = """
            {
                "code": 201,
                "data": {
                    "nombre": "Maria",
                    "apellido": "Martinez",
                    "email": "email2@email.com",
                    "perfil": "CLIENT",
                    "activo": true
                }
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any()) } returns client
        val res = controller.createUser(users[0], adminToken)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createUserIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any())} returns client
        val res = controller.createUser(users[0], "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun createUserIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns null
        coEvery { repoUser.findByEmail(any()) } returns null
        coEvery { repoUser.findByPhone(any()) } returns null
        coEvery { repoUser.save(any())} returns client
        val res = controller.createUser(users[0], userToken)

        assertAll( { assertEquals(response, res)} )
    }

    @Test
    fun deleteUserCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "nombre": "Maria",
                    "apellido": "Martinez",
                    "email": "email2@email.com",
                    "perfil": "CLIENT",
                    "activo": true
                }
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteUserIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot delete user with id 93a98d69-0006-48a7-b34f-05b596ea839a."
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns null
        var result = ""
        launch { result = controller.deleteUser(client.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteUserIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun deleteUserIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.delete(client.id) } returns client
        var result = ""
        launch { result = controller.deleteUser(client.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveUserCorrect() = runTest {
        val response = """
            {
                "code": 200,
                "data": {
                    "nombre": "Maria",
                    "apellido": "Martinez",
                    "email": "email2@email.com",
                    "perfil": "CLIENT",
                    "activo": true
                }
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveUserIncorrect() = runTest {
        val response = """
            {"code":401,"message":"UNAUTHORIZED: No token detected"}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid, "") }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveUserIncorrectToken() = runTest {
        val response = """
            {"code":403,"message":"FORBIDDEN: You are not allowed to to this."}
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns client
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid, userToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }

    @Test
    fun setInactiveUserIncorrectNull() = runTest {
        val response = """
            {
                "code": 500,
                "message": "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive user with id ${client.uuid}."
            }
        """.trimIndent()
        coEvery { repoUser.findByUUID(any()) } returns client
        coEvery { repoUser.setInactive(client.id)} returns null
        var result = ""
        launch { result = controller.setInactiveUser(client.uuid, adminToken) }.join()

        assertAll(
            { assertEquals(response, result) }
        )
    }
}