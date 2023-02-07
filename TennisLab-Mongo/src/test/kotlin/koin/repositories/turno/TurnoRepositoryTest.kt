package koin.repositories.turno

import koin.db.DBManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import koin.models.turno.Turno
import koin.models.user.User
import kotlinx.coroutines.launch
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.litote.kmongo.newId
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.List

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TurnoRepositoryTest {
    private val repository = TurnoRepository()

    private val entity = Turno(
        uuid = UUID.fromString("93a98d69-0019-48a7-b34f-05b596ea8abc"),
        workerId = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        maquinaId = UUID.fromString("93a98d69-0008-48a7-b34f-05b596ea83bb"),
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = LocalDateTime.of(2002, 10, 14, 16, 49),
        numPedidosActivos = 2,
        tarea1Id = UUID.fromString("93a98d69-0015-48a7-b34f-05b596ea8aab"),
        tarea2Id = UUID.fromString("93a98d69-0011-48a7-b34f-05b596ea83ca"),
        finalizado = false
    )

    /**
     * Inicializacion de la base de datos para testing y carga de datos necesarios.
     */
    companion object {
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<Turno>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<Turno>().save(entity)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<Turno>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: Turno? = null
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
        var result: List<Turno>? = null
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
        var result: Turno? = null
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
        var result: Turno? = null
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
    fun setFinalizado() = runTest {
        var result: Turno? = null
        launch { result = repository.setFinalizado(entity.id) }.join()

        assertAll(
            { assertNotNull(result) },
            { Assertions.assertTrue { result!!.finalizado } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun delete() = runTest {
        var result: Turno? = null
        launch { result = repository.delete(entity.id) }.join()

        assertAll(
            { assertEquals(result?.uuid, entity.uuid) },
            { assertEquals(result?.id, entity.id) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun findByIdNotExists() = runTest {
        var result: Turno? = null
        launch { result = repository.findById(newId()) }.join()

        Assertions.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun deleteNotExists() = runTest {
        var result: Turno? = null
        launch { result = repository.delete(newId()) }.join()

        Assertions.assertNull(result)
    }
}