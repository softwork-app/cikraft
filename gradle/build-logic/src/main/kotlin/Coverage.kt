import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@XmlSerialName("coverage")
data class Coverage @OptIn(ExperimentalTime::class) constructor(
    @SerialName("line-rate")
    val lineRate: Double,

    @SerialName("branch-rate")
    val branchRate: Double,

    @SerialName("lines-covered")
    val linesCovered: Long,

    @SerialName("lines-valid")
    val linesValid: Long,

    @SerialName("branches-covered")
    val branchesCovered: Long,

    @SerialName("branches-valid")
    val branchesValid: Long,

    val complexity: Double = 0.0,
    val version: String,

    @Serializable(UnixEpochSecondsSerializer::class)
    val timestamp: Instant,

    @XmlElement(true)
    val sources: Sources? = null,

    @XmlElement(true)
    val packages: Packages,
)

@OptIn(ExperimentalTime::class)
private data object UnixEpochSecondsSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UnixEpochSecondsSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSeconds)
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.fromEpochSeconds(decoder.decodeLong())
    }
}

@Serializable
@XmlSerialName("sources")
data class Sources(
    @XmlElement(true)
    val source: List<Source> = emptyList(),
)

@Serializable
@XmlSerialName("source")
data class Source(
    @XmlValue(true)
    val value: String,
)

@Serializable
@XmlSerialName("packages")
data class Packages(
    @XmlElement(true)
    @SerialName("package")
    val packages: List<Package> = emptyList(),
)

@Serializable
@XmlSerialName("package")
data class Package(
    val name: String,

    @SerialName("line-rate")
    val lineRate: Double,

    @SerialName("branch-rate")
    val branchRate: Double,

    val complexity: Double = 0.0,

    @XmlElement(true)
    val classes: Classes,
)

@Serializable
@XmlSerialName("classes")
data class Classes(
    @XmlElement(true)
    @SerialName("class")
    val classes: List<Clazz> = emptyList(),
)

@Serializable
@XmlSerialName("class")
data class Clazz(
    val name: String,
    val filename: String,

    @SerialName("line-rate")
    val lineRate: Double,

    @SerialName("branch-rate")
    val branchRate: Double,

    val complexity: Double = 0.0,

    @XmlElement(true)
    val methods: Methods,

    @XmlElement(true)
    val lines: Lines,
)

@Serializable
@XmlSerialName("methods")
data class Methods(
    @XmlElement(true)
    val method: List<Method> = emptyList(),
)

@Serializable
@XmlSerialName("method")
data class Method(
    val name: String,
    val signature: String,

    @SerialName("line-rate")
    val lineRate: Double,

    @SerialName("branch-rate")
    val branchRate: Double,

    val complexity: Double = 0.0,

    @XmlElement(true)
    val lines: Lines,
)

@Serializable
@XmlSerialName("lines")
data class Lines(
    @XmlElement(true)
    val line: List<Line> = emptyList(),
)

@Serializable
@XmlSerialName("line")
data class Line(
    val number: String,
    val hits: Long,

    val branch: String = "false",

    @SerialName("condition-coverage")
    val conditionCoverage: String = "100%",

    @XmlElement(true)
    val conditions: Conditions? = null,
)

@Serializable
@XmlSerialName("conditions")
data class Conditions(
    @XmlElement(true)
    val condition: List<Condition> = emptyList(),
)

@Serializable
@XmlSerialName("condition")
data class Condition(
    val number: String,
    val type: String,
    val coverage: String,
) {
}
