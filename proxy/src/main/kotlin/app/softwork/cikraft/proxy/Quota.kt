package app.softwork.cikraft.proxy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("Quota", namespace = "http://www.sap.com/apimgmt")
public data class Quota(
    override val async: Boolean = false,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    val type: String = "calendar",
    @SerialName("Allow")
    val allow: Allow,
    @SerialName("Interval")
    val interval: Interval,
    @SerialName("Distributed")
    val distributed: Distributed,
    @SerialName("StartTime")
    @XmlElement
    val startTime: String,
    @SerialName("Synchronous")
    @XmlElement
    val synchronous: Boolean,
    @SerialName("TimeUnit")
    @XmlElement
    val timeUnit: TimeUnit,
) : Policy {
    @Serializable
    public data class Allow(val count: Int)

    @Serializable
    public data class Interval(
        @XmlValue
        val count: Int,
    )

    @Serializable
    public data class Distributed(
        @XmlValue
        val value: Boolean,
    )

    @Serializable
    public enum class TimeUnit {
        @SerialName("second")
        Second,

        @SerialName("minute")
        Minute,

        @SerialName("hour")
        Hour,

        @SerialName("day")
        Day,

        @SerialName("month")
        Month,
    }
}
