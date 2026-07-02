package app.softwork.cikraft.proxy

import kotlinx.serialization.Serializable

@Serializable
public enum class ServiceCode {
    REST,
    ODATA,
    SOAP,
}
