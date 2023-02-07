package koin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

/**
 * @author Daniel Rodriguez Muñoz
 * Clase para que kotlinx-serialization pueda serializar los LocalDates
 */
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    /**
     * Deserializa el LocalDate: El decoder pasa la cadena serializada a su representacion string y
     * esta la pasamos como parametro al metodo parse de la clase LocalDate,
     * de tal manera que obtenemos el LocalDate.
     */
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }

    /**
     * Pasamos el LocalDate en forma de String al encoder (mediante un toString)
     * y este mediante el metodo encodeString lo serializa.
     */
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}