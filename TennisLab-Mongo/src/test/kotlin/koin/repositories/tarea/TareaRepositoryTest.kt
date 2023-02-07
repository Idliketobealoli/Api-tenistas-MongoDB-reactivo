package koin.repositories.tarea

import koin.db.DBManager
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.models.turno.Turno
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TareaRepositoryTest {
    private val repository = TareaRepository()

    private val entity = Tarea(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0013-48a7-b34f-05b596ea83cc"),
        raquetaId = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        precio = 0.0,
        tipo = TipoTarea.ADQUISICION,
        finalizada = false,
        pedidoId = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        productoAdquiridoId = UUID.fromString("93a98d69-0004-48a7-b34f-05b596ea83aa"),
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<Tarea>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<Tarea>().save(entity)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<Tarea>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: Tarea? = null
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
        var result: List<Tarea>? = null
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
        var result: Tarea? = null
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
        var result: Tarea? = null
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
    fun setFinalizada() = runTest {
        var result: Tarea? = null
        launch { result = repository.setFinalizada(entity.id) }.join()

        assertAll(
            { assertNotNull(result) },
            { Assertions.assertTrue { result!!.finalizada } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun delete() = runTest {
        var result: Tarea? = null
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
        var result: Tarea? = null
        launch { result = repository.findById(newId()) }.join()

        Assertions.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun deleteNotExists() = runTest {
        var result: Tarea? = null
        launch { result = repository.delete(newId()) }.join()

        Assertions.assertNull(result)
    }
}