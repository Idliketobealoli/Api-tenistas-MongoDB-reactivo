package koin.repositories.user

import koin.db.DBManager
import koin.models.user.User
import koin.models.user.UserProfile
import koin.services.utils.toUUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {
    private val repository = UserRepository()

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
    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<User>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<User>().save(user)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<User>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.save(user) }
        job.join()

        assertAll(
            { assertEquals(result?.id, user.id) },
            { assertEquals(result?.uuid, user.uuid) },
            { assertEquals(result?.nombre, user.nombre) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(2)
    fun findAll() = runTest {
        var result: List<User>? = null
        val job: Job = launch { result = repository.findAll().toList() }
        job.join()

        assertAll(
            { assertNotNull(result) },
            { assertEquals("93a98d69-0000-1111-0000-05b596ea83ba".toUUID(), result?.get(0)?.uuid) },
            { assertEquals(1, result?.size) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(3)
    fun findById() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.findById(user.id) }
        job.join()

        Assertions.assertAll(
            { assertEquals(user.id, result?.id) },
            { assertEquals("93a98d69-0000-1111-0000-05b596ea83ba".toUUID(), result?.uuid) },
            { assertEquals("loli", result?.nombre) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(4)
    fun findByUuid() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.findByUUID(user.uuid) }
        job.join()

        assertAll(
            { assertNotNull(result) },
            { assertEquals(user.id, result?.id) },
            { assertEquals("loli", result?.nombre) }
        )
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(5)
    fun setInactive() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.setInactive(user.id) }
        job.join()

        assertAll(
            { assertNotNull(result) },
            { assertFalse { result!!.activo } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun delete() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.delete(user.id) }
        job.join()

        assertAll(
            { assertEquals(result?.uuid, user.uuid) },
            { assertEquals(result?.nombre, user.nombre) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun findByIdNotExists() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.findById(newId()) }
        job.join()

        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun deleteNotExists() = runTest {
        var result: User? = null
        val job: Job = launch { result = repository.delete(newId()) }
        job.join()

        assertNull(result)
    }
}