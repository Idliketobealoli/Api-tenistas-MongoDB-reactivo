package koin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Clase para que kotlinx-serialization pueda serializar los UUIDs
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    /**
     * Deserializa el UUID: El decoder pasa la cadena serializada a su representacion string y
     * esta la pasamos como parametro al metodo fromString de la clase UUID,
     * de tal manera que obtenemos el UUID.
     */
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    /**
     * Pasamos el UUID en forma de String al encoder (mediante un toString)
     * y este mediante el metodo encodeString lo serializa.
     */
    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}