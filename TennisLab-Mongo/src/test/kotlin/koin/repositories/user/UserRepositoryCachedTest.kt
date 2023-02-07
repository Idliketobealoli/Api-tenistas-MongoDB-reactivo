package koin.repositories.user

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import koin.db.DBManager
import koin.models.user.User
import koin.models.user.UserProfile
import koin.services.cache.user.UserCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.newId
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UserRepositoryCachedTest {

    private val user = User(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0000-1111-0000-05b596ea83ba"),
        nombre = "loli",
        apellido = "test",
        telefono = "123456789",
        email = "loli@test.com",
        password = "lolitest",
        perfil = UserProfile.ADMIN,
        activo = true
    )

    @MockK
    lateinit var repo: UserRepository

    @SpyK
    var cache = UserCache()

    @InjectMockKs
    lateinit var repository: UserRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(2)
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findAll().toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { assertEquals(user.uuid, result[0].uuid) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(4)
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(user.uuid) } returns user

        val result = repository.findByUUID(user.uuid)

        assertAll(
            { Assertions.assertEquals(user.id, result?.id) },
            { assertEquals(user.uuid, result?.uuid)},
            { assertEquals(user.nombre, result?.nombre)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(3)
    fun findById() = runTest {
        coEvery { repo.findById(user.id) } returns user

        val result = repository.findById(user.id)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.uuid, result?.uuid)},
            { assertEquals(user.nombre, result?.nombre)}
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(5)
    fun findByIdNumerical() = runTest {
        val result = repository.findById(1)

        assertAll(
            { assertEquals("Leanne", result?.nombre)}
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(12)
    fun findByIdNotExists() = runTest {
        coEvery{ repo.findById(any())} returns null

        val result = repository.findById(newId())
        assertNull(result)

        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(11)
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
        coEvery { repo.save(any()) } returns user

        val result = repository.save(user)

        assertAll(
            { assertEquals(user.uuid, result.uuid) },
            { assertEquals(user.nombre, result.nombre) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(10)
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.delete(any()) } returns user

        val result = repository.delete(user.id)!!

        assertAll(
            { assertEquals(user.uuid, result.uuid) },
            { assertEquals(user.nombre, result.nombre) },
        )

        coVerify { repo.delete(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(9)
    fun setInactive() = runTest {
        coEvery { repo.setInactive(any()) } returns user

        val result = repository.setInactive(user.id)

        assertAll(
            { assertEquals(user.uuid, result?.uuid) },
            { assertEquals(user.nombre, result?.nombre) },
        )
        coVerify { repo.setInactive(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun findByEmail() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findByEmail(user.email)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.uuid, result?.uuid)},
            { assertEquals(user.nombre, result?.nombre)}
        )
        coVerify {repo.findAll()}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun findByPhone() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findByPhone(user.telefono)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.uuid, result?.uuid)},
            { assertEquals(user.nombre, result?.nombre)}
        )
        coVerify {repo.findAll()}
    }
}