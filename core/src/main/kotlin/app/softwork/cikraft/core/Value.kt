package app.softwork.cikraft.core

import kotlinx.serialization.Serializable

@Serializable
public sealed interface Value {
    @Serializable
    public class STRING(public val value: String) : Value

    @Serializable
    public class INT(public val value: Int) : Value

    @Serializable
    public class FLOAT(public val value: Float) : Value

    @Serializable
    public class DOUBLE(public val value: Double) : Value

    @Serializable
    public class BOOLEAN(public val value: Boolean) : Value
}
