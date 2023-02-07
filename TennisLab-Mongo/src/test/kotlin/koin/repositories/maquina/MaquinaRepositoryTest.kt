package koin.repositories.maquina

import koin.db.DBManager
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
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
class MaquinaRepositoryTest {
    private val repository = MaquinaRepository()

    private val entity = Maquina(
        id = newId(),
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83bc"),
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
    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<Maquina>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<Maquina>().save(entity)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<Maquina>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: Maquina? = null
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
        var result: List<Maquina>? = null
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
        var result: Maquina? = null
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
        var result: Maquina? = null
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
    fun setInactive() = runTest {
        var result: Maquina? = null
        launch { result = repository.setInactive(entity.id) }.join()

        assertAll(
            { assertNotNull(result) },
            { Assertions.assertFalse { result!!.activa } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun delete() = runTest {
        var result: Maquina? = null
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
        var result: Maquina? = null
        launch { result = repository.findById(newId()) }.join()

        Assertions.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun deleteNotExists() = runTest {
        var result: Maquina? = null
        launch { result = repository.delete(newId()) }.join()

        Assertions.assertNull(result)
    }
}