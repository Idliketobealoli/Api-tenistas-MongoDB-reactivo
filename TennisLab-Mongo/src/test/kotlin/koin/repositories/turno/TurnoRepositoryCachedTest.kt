package koin.repositories.turno

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import koin.models.turno.Turno
import koin.services.cache.turno.TurnoCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.newId
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class TurnoRepositoryCachedTest {
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

    @MockK
    lateinit var repo: TurnoRepository

    @SpyK
    var cache = TurnoCache()

    @InjectMockKs
    lateinit var repository: TurnoRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(2)
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(entity)

        val result = repository.findAll().toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { assertEquals(entity.uuid, result[0].uuid) },
            { Assertions.assertEquals(1, result.size) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(4)
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(entity.uuid) } returns entity

        val result = repository.findByUUID(entity.uuid)

        assertAll(
            { Assertions.assertEquals(entity.id, result?.id) },
            { assertEquals(entity.uuid, result?.uuid)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(3)
    fun findById() = runTest {
        coEvery { repo.findById(entity.id) } returns entity

        val result = repository.findById(entity.id)

        assertAll(
            { assertEquals(entity.id, result?.id)},
            { assertEquals(entity.uuid, result?.uuid)}
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun findByIdNotExists() = runTest {
        coEvery{ repo.findById(any())} returns null

        val result = repository.findById(newId())
        assertNull(result)

        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun findByUUIDNotExists() = runTest {
        coEvery{ repo.findByUUID(any())} returns null

        val result = repository.findByUUID(UUID.randomUUID())
        assertNull(result)

        coVerify {repo.findByUUID(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        coEvery { repo.save(any()) } returns entity

        val result = repository.save(entity)

        assertAll(
            { assertEquals(entity.uuid, result.uuid) }
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns entity
        coEvery { repo.delete(any()) } returns entity

        val result = repository.delete(entity.id)!!

        assertAll(
            { assertEquals(entity.uuid, result.uuid) }
        )

        coVerify { repo.delete(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(5)
    fun setFinalizado() = runTest {
        coEvery { repo.setFinalizado(any()) } returns entity

        val result = repository.setFinalizado(entity.id)

        assertAll(
            { assertEquals(entity.uuid, result?.uuid) }
        )
        coVerify { repo.setFinalizado(any()) }
    }
}