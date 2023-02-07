package koin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

/**
 * @author Daniel Rodriguez Muñoz
 * Clase para que kotlinx-serialization pueda serializar los LocalDateTimes
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    /**
     * Deserializa el LocalDateTime: El decoder pasa la cadena serializada a su representacion string y
     * esta la pasamos como parametro al metodo parse de la clase LocalDateTime,
     * de tal manera que obtenemos el LocalDateTime.
     */
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }

    /**
     * Pasamos el LocalDateTime en forma de String al encoder (mediante un toString)
     * y este mediante el metodo encodeString lo serializa.
     */
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}