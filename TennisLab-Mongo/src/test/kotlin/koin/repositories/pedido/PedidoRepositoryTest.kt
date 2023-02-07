package koin.repositories.pedido

import koin.db.DBManager
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.turno.Turno
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PedidoRepositoryTest {
    private val repository = PedidoRepository()

    private val entity = Pedido(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = UUID.fromString("93a98d69-0006-48a7-b34f-05b596ea839a"),
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        precio = 0.0
    )
    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<Pedido>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<Pedido>().save(entity)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<Pedido>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: Pedido? = null
        launch { result = repository.save(entity) }.join()

        assertAll(
            { assertEquals(result?.id, entity.id) },
            { assertEquals(result?.uuid, entity.uuid) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(2)
    fun findAll() = runTest {
        var result: List<Pedido>? = null
        launch { result = repository.findAll().toList() }.join()


        assertAll(
            { assertNotNull(result) },
            { assertEquals(entity.uuid, result?.get(0)?.uuid) },
            { assertEquals(entity.id, result?.get(0)?.id) },
            { assertEquals(1, result?.size) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(3)
    fun findById() = runTest {
        var result: Pedido? = null
        launch { result = repository.findById(entity.id) }.join()

        Assertions.assertAll(
            { assertEquals(entity.id, result?.id) },
            { assertEquals(entity.uuid, result?.uuid) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(4)
    fun findByUuid() = runTest {
        var result: Pedido? = null
        launch { result = repository.findByUUID(entity.uuid) }.join()

        assertAll(
            { assertNotNull(result) },
            { assertEquals(entity.id, result?.id) },
            { assertEquals(entity.uuid, result?.uuid) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(5)
    fun delete() = runTest {
        var result: Pedido? = null
        launch { result = repository.delete(entity.id) }.join()

        assertAll(
            { assertEquals(result?.uuid, entity.uuid) },
            { assertEquals(result?.id, entity.id) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun findByIdNotExists() = runTest {
        var result: Pedido? = null
        launch { result = repository.findById(newId()) }.join()

        Assertions.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun deleteNotExists() = runTest {
        var result: Pedido? = null
        launch { result = repository.delete(newId()) }.join()

        Assertions.assertNull(result)
    }
}