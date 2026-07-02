package app.softwork.cikraft.api.proxy

import app.softwork.cikraft.api.Deferred
import app.softwork.cikraft.api.LifeCycle
import app.softwork.cikraft.api.Metadata
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

@Serializable
public data class Credentials(
    @SerialName("__metadata")
    val metadata: Metadata? = null,
    val name: String,
    val storeName: String,
    val certName: String,
    val csKey: String? = null,
    val description: String? = null,
    val format: Certificate.Format,
    val password: String? = null,
    val storeType: New.StoreType? = null,
    @Serializable(InstantMilliseconds::class) val expiryDate: Instant? = null,
    @Serializable(InstantMilliseconds::class) val validFrom: Instant? = null,
    val issuerEmail: String?,
    val subjectEmail: String?,
    val signAlgName: String?,
    val isValid: String?,
    val version: String?,
    val content: String? = null,
    val certificateStore: Deferred,
    @SerialName("life_cycle")
    val lifeCycle: LifeCycle,
) {
    @Serializable
    public data class Created(
        @SerialName("__metadata")
        val metadata: Metadata,
        @SerialName("life_cycle")
        val lifeCycle: LifeCycle,
        val name: String,
        val storeType: New.StoreType,
        val certificates: Deferred,
    )

    @Serializable
    public data class New(
        @SerialName("name")
        val storeName: String,
        val storeType: StoreType,
        val certificates: List<Certificate>,
        val description: String? = null,
    ) {
        public enum class StoreType {
            @SerialName("KS")
            KeyStore,
        }
    }

    @Serializable
    public data class Certificate(
        val name: String,
        val description: String? = null,
        val format: Format,
        @SerialName("content")
        val base64Content: String,
        val password: String,
    ) {
        @Serializable
        public enum class Format {
            @SerialName("PCKS12")
            PKCS12,
        }
    }
}

private data object InstantMilliseconds : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantMilliseconds", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant = Instant.fromEpochMilliseconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }
}
