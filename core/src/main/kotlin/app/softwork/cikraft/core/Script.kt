package app.softwork.cikraft.core

import io.github.hfhbd.kfx.codegen.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.Enum
import io.github.hfhbd.kfx.codegen.CodeGenTree.Expression.*
import kotlinx.serialization.*

@Serializable
public data class Script(
    val name: String,
    val isSuspend: Boolean,
    val jvmFunction: String,
    val inputs: List<Input>,
    val outputJvmName: String,
    val outputIsNullable: Boolean,
    val outputs: Set<Output>,
    val error: Error?,
) {
    public val packageName: String = getPackageName(jvmFunction)

    public val bodyInput: Body? = inputs.singleOrNull {
        it is Body
    } as Body?

    public val bodyOutput: Body? = outputs.singleOrNull {
        it is Body
    } as Body?

    @Serializable
    public sealed interface Input {
        public val propertyName: String
        public val klass: Type
        public val nullable: Boolean
        public val documentation: String?
    }

    @Serializable
    public data class Body(
        override val propertyName: String,
        override val klass: Type,
        override val nullable: Boolean,
        val contentNegotiations: List<ContentNegotiation>,
        val sealedSubClasses: Set<SealedSubClass>,
        override val documentation: String? = null,
    ) : Input,
        Output {
        @Serializable
        public data class ContentNegotiation(
            val factoryKlass: NormalClass,
            val contentType: String,
            val parameters: Map<String, String>,
        )
    }

    @Serializable
    public data class SealedSubClass(val parent: ClassName, val klass: NormalClass)

    @Serializable
    public data class Header(
        val name: String,
        override val propertyName: String,
        override val klass: Type.Builtin,
        override val nullable: Boolean,
        override val documentation: String? = null,
    ) : Input,
        Output

    @Serializable
    public data class None(
        override val propertyName: String,
        override val klass: Type,
        override val nullable: Boolean,
        override val documentation: String? = null,
        val hasDefault: Boolean,
    ) : Input

    public sealed interface ParameterInput : Input

    @Serializable
    public data class Password(
        override val propertyName: String,
        override val nullable: Boolean,
        override val documentation: String? = null,
    ) : ParameterInput {
        override val klass: Type.Builtin.CHARARRAY = Type.Builtin.CHARARRAY
    }

    @Serializable
    public data class Parameter(
        override val propertyName: String,
        override val klass: Type.Builtin,
        override val nullable: Boolean,
        override val documentation: String? = null,
    ) : ParameterInput

    @Serializable
    public data class Property(
        val name: String,
        override val propertyName: String,
        override val klass: Type,
        override val nullable: Boolean,
        override val documentation: String? = null,
    ) : ParameterInput,
        Output

    @Serializable
    public sealed interface Output {
        public val propertyName: String
        public val klass: Type
        public val nullable: Boolean
        public val documentation: String?
    }

    @Serializable
    public data class DynamicHeaders(override val propertyName: String, override val documentation: String? = null) :
        Output {
        override val klass: Type.MAP = Type.MAP(Type.Builtin.STRING, Type.Builtin.STRING)
        override val nullable: Boolean = false
    }

    @Serializable
    public data class Error(
        val packageName: String,
        val name: String,
        val outputs: Set<Output>,
        val documentation: String? = null,
    ) {
        public val bodyOutput: Body = outputs.single {
            it is Body
        } as Body
    }
}

public val Member.minLength: Int?
    get() = (
        annotations.singleOrNull {
            it.packageName == VALIDATION_PACKAGENAME && it.names == listOf("MinLength")
        }?.values[VALIDATION_INCLUSIVE] as IntLiteral?
        )?.value
public val Member.maxLength: Int?
    get() = (
        annotations.singleOrNull {
            it.packageName == VALIDATION_PACKAGENAME && it.names == listOf("MaxLength")
        }?.values[VALIDATION_INCLUSIVE] as IntLiteral?
        )?.value

public val Member.serialName: String? get() = annotations.serialName
public val Enum.Value.serialName: String? get() = annotations.serialName
public val Class.serialName: String? get() = annotations.serialName
private val List<CodeGenTree.Annotation>.serialName: String?
    get() = (
        singleOrNull {
            it.packageName == "kotlinx.serialization" && it.names == listOf("SerialName")
        }?.values["value"] as StringLiteral?
        )?.value

private const val VALIDATION_PACKAGENAME = "app.softwork.validation"
private const val VALIDATION_INCLUSIVE = "inclusive"

internal fun getPackageName(jvmFunction: String): String =
    jvmFunction.split(".").takeUnless { it.size == 2 }?.dropLast(2)?.joinToString(".") ?: ""
