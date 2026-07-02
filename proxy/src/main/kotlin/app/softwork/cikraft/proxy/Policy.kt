package app.softwork.cikraft.proxy

import kotlinx.serialization.Serializable

@Serializable
public sealed interface Policy {
    public val async: Boolean
    public val continueOnError: Boolean
    public val enabled: Boolean
}
