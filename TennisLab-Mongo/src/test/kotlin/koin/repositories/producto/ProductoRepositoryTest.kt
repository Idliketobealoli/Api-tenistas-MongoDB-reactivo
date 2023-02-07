package koin.repositories.producto

import koin.db.DBManager
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
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
class ProductoRepositoryTest {
    private val repository = ProductoRepository()

    private val entity = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 1
    )

    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = DBManager.database.getCollection<Producto>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = DBManager.database.getCollection<Producto>().save(entity)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = DBManager.database.getCollection<Producto>().drop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(1)
    fun save() = runTest {
        var result: Producto? = null
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
        var result: List<Producto>? = null
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
        var result: Producto? = null
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
        var result: Producto? = null
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
    fun decreaseStock() = runTest {
        var result: Producto? = null
        launch { result = repository.decreaseStock(entity.id) }.join()

        assertAll(
            { assertNotNull(result) },
            { Assertions.assertTrue { result!!.stock == 0 } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(6)
    fun decreaseStockBelow0() = runTest {
        var result: Producto? = null
        launch { result = repository.decreaseStock(entity.id) }.join()

        assertAll(
            { assertNotNull(result) },
            { Assertions.assertTrue { result!!.stock == 0 } }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(7)
    fun delete() = runTest {
        var result: Producto? = null
        launch { result = repository.delete(entity.id) }.join()

        assertAll(
            { assertEquals(result?.uuid, entity.uuid) },
            { assertEquals(result?.id, entity.id) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(8)
    fun findByIdNotExists() = runTest {
        var result: Producto? = null
        launch { result = repository.findById(newId()) }.join()

        Assertions.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Order(9)
    fun deleteNotExists() = runTest {
        var result: Producto? = null
        launch { result = repository.delete(newId()) }.join()

        Assertions.assertNull(result)
    }
}