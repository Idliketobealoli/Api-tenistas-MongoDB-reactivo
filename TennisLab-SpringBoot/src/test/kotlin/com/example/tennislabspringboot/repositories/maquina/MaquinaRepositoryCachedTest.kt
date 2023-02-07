package com.example.tennislabspringboot.repositories.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import com.example.tennislabspringboot.models.maquina.TipoMaquina
import com.example.tennislabspringboot.services.cache.maquina.MaquinaCache
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class MaquinaRepositoryCachedTest {
    private val entity = Maquina(
        id = ObjectId.get(),
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

    @MockK
    lateinit var repo: MaquinaRepository

    @SpyK
    var cache = MaquinaCache()

    @InjectMockKs
    lateinit var repository: MaquinaRepositoryCached

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
        coEvery { repo.findFirstByUuid(entity.uuid) } returns flowOf(entity)

        val result = repository.findByUUID(entity.uuid)

        assertAll(
            { Assertions.assertEquals(entity.id, result?.id) },
            { assertEquals(entity.uuid, result?.uuid)}
        )
        coVerify {repo.findFirstByUuid(any())}
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

        val result = repository.findById(ObjectId.get())
        assertNull(result)

        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun findByUUIDNotExists() = runTest {
        coEvery{ repo.findFirstByUuid(any())} returns flowOf()

        val result = repository.findByUUID(UUID.randomUUID())
        assertNull(result)

        coVerify {repo.findFirstByUuid(any())}
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
        coEvery { repo.delete(any()) } returns Unit

        val result = repository.delete(entity.id)!!

        assertAll(
            { assertEquals(entity.uuid, result.uuid) }
        )

        coVerify { repo.delete(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(5)
    fun setInactive() = runTest {
        coEvery { repo.findById(any()) } returns entity
        coEvery { repo.save(any()) } returns entity

        val result = repository.setInactive(entity.id)

        assertAll(
            { assertEquals(entity.uuid, result?.uuid) }
        )
        coVerify { repo.findById(any()) }
    }
}