package app.softwork.cikraft.generator

import app.softwork.cikraft.core.Script
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.joinToCode
import io.github.hfhbd.kfx.codegen.CodeGenTree
import io.github.hfhbd.kfx.codegen.CodeGenTree.NormalClass
import io.github.hfhbd.kfx.codegen.CodeGenTree.Type
import io.github.hfhbd.kfx.kotlin.toPoetType

public fun writeKotlinEntryPoints(
    scripts: List<Script>,
): FileSpec = FileSpec.builder("", "CiKraftEntrypoints").apply {
    for (entryPoint in scripts) {
        with(entryPoint) {
            addFunction(
                FunSpec.builder(name).apply {
                    receiver(MESSAGE)
                    addParameter("messageLog", MESSAGE_LOG)
                    returns(MESSAGE)

                    val bodyOutput = entryPoint.bodyOutput?.takeUnless {
                        it.contentNegotiations.isEmpty()
                    }
                    val error = error

                    if (bodyOutput != null || error != null) {
                        validateAcceptHeader(bodyOutput, error?.bodyOutput)
                    }

                    val bodyInput = entryPoint.bodyInput
                    if (bodyInput != null) {
                        validateContentType(bodyInput)
                    }

                    if (error != null) {
                        beginControlFlow("try")
                    }

                    callMethod(entryPoint)

                    if (outputJvmName != "kotlin.Unit") {
                        addOutputs(outputs, "output", isError = false)
                    }

                    if (error != null) {
                        val errorClass = ClassName(
                            error.packageName,
                            error.name,
                        )
                        nextControlFlow("catch (error: %T)", errorClass)
                        addOutputs(error.outputs, "error", isError = true)
                        endControlFlow()
                    }

                    addStatement("return this")
                }.build(),
            )
        }
    }
}.build()

private fun FunSpec.Builder.validateAcceptHeader(
    output: Script.Body?,
    errorOutput: Script.Body?,
) {
    beginControlFlow(
        "val acceptHeader: %T = getHeader(%S, %T::class.java)?.split(%S)?.map",
        LIST.parameterizedBy(
            ClassName("kotlin", "Pair").parameterizedBy(
                STRING,
                MAP.parameterizedBy(STRING, STRING),
            ),
        ),
        "Accept",
        STRING,
        ",",
    )
    addStatement("val split = it.trim().split(%S)", ";")
    beginControlFlow("val parameters = split.drop(1).associate")
    addStatement("val (key, value) = it.trim().split(%S)", "=")
    addStatement("key to value")
    endControlFlow()
    addStatement("split[0] to parameters")
    nextControlFlowFixed("?.sortedBy { (_, parameters) ->")
    addStatement("parameters[%S]?.toDouble() ?: %L", "q", 1.0)
    endControlFlow("?: listOf(%S to emptyMap())", "*/*")

    if (output != null) {
        getResponseFactory(output, "response")
    }
    if (errorOutput != null) {
        getErrorResponseFactory(errorOutput, "error")
    }
}

private fun String.withOpeningBrace(): String {
    for (i in lastIndex downTo 0) {
        if (this[i] == '{') {
            return "$this\n"
        } else if (this[i] == '}') {
            break
        }
    }
    return "$this {\n"
}

private fun FunSpec.Builder.nextControlFlowFixed(controlFlow: String, vararg args: Any?): FunSpec.Builder = addCode(
    "%L",
    CodeBlock.Builder().apply {
        unindent()
        add("}${controlFlow.withOpeningBrace()}", *args)
        indent()
    }.build(),
)

private fun FunSpec.Builder.endControlFlow(controlFlow: String?, vararg args: Any?): FunSpec.Builder = addCode(
    "%L",
    CodeBlock.Builder().apply {
        unindent()
        add("}")
        if (controlFlow != null) {
            add(" $controlFlow", args = args)
        }
        add("\n")
    }.build(),
)

private fun FunSpec.Builder.getResponseFactory(
    output: Script.Body,
    prefix: String,
) {
    val first = output.contentNegotiations.first()

    addStatement("var ${prefix}Factory: %T = null", STRING_FORMAT.copy(nullable = true))
    addStatement("var ${prefix}ContentType: %T = null", STRING.copy(nullable = true))

    beginControlFlow("for ((contentType, parameters) in acceptHeader)")
    beginControlFlow("when (contentType)")

    beginControlFlow("%S ->", "*/*")
    setContentType(prefix, first)
    addStatement("break")
    endControlFlow()

    for (contentNegotiation in output.contentNegotiations) {
        checkContentTypeParameters(contentNegotiation)
        setContentType(prefix, contentNegotiation)
        addStatement("break")
        endControlFlow()
    }
    endControlFlow()
    endControlFlow()

    beginControlFlow("if (${prefix}Factory == null || ${prefix}ContentType == null)")
    addStatement("setHeader(%S, 406)", STATUS)
    addStatement("body = null")
    addStatement("return this")
    endControlFlow()
}

private fun FunSpec.Builder.getErrorResponseFactory(
    output: Script.Body,
    prefix: String,
) {
    val first = output.contentNegotiations.first()

    addStatement("var ${prefix}Factory: %T = null", STRING_FORMAT.copy(nullable = true))
    addStatement("var ${prefix}ContentType: %T = null", STRING.copy(nullable = true))

    beginControlFlow("for ((contentType, parameters) in acceptHeader)")
    beginControlFlow("when (contentType)")

    beginControlFlow("%S ->", "*/*")
    setContentType(prefix, first)
    addStatement("break")
    endControlFlow()

    for (contentNegotiation in output.contentNegotiations) {
        checkContentTypeParameters(contentNegotiation)
        setContentType(prefix, contentNegotiation)
        addStatement("break")
        endControlFlow()
    }
    endControlFlow()
    endControlFlow()

    beginControlFlow("if (${prefix}Factory == null || ${prefix}ContentType == null)")
    setContentType(prefix, first)
    endControlFlow()
}

private fun FunSpec.Builder.setContentType(
    prefix: String,
    contentNegotiation: Script.Body.ContentNegotiation,
) {
    addStatement(
        "${prefix}Factory = %T%L",
        contentNegotiation.factoryKlass.toKotlinPoet(false),
        CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
    )
    addStatement("${prefix}ContentType = %S", contentNegotiation.contentType())
}

private fun FunSpec.Builder.checkContentTypeParameters(contentNegotiation: Script.Body.ContentNegotiation) {
    beginControlFlow(
        "%S%L ->",
        contentNegotiation.contentType,
        contentNegotiation.parameters.map { param ->
            CodeBlock.of(
                "(parameters[%S] == null || parameters[%S] == %S)",
                param.key,
                param.key,
                param.value,
            )
        }.joinToCode(prefix = if (contentNegotiation.parameters.isEmpty()) "" else " if ", separator = " && "),
    )
}

private fun FunSpec.Builder.validateContentType(
    input: Script.Body,
) {
    if (input.contentNegotiations.isEmpty()) {
        return
    }

    beginControlFlow(
        "val (contentType, parameters) = getHeader(%S, %T::class.java)?.split(%S)?.let",
        "Content-Type",
        STRING,
        ";",
    )
    addStatement("val contentType = it[0]")
    beginControlFlow("val parameters = it.drop(1).associate")
    addStatement("val (key, value) = it.split(%S)", "=")
    addStatement("key to value")
    endControlFlow()
    addStatement("contentType to parameters")
    endControlFlow("?: (null to emptyMap())")
    addStatement("val requestFactory: %T", STRING_FORMAT)
    beginControlFlow("when (contentType)")
    for (contentNegotiation in input.contentNegotiations) {
        checkContentTypeParameters(contentNegotiation)
        addStatement(
            "requestFactory = %T%L",
            contentNegotiation.factoryKlass.toKotlinPoet(false),
            CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
        )
        endControlFlow()
    }
    beginControlFlow("else ->")
    addStatement("setHeader(%S, %S)", "Accept-Post", input.contentNegotiations.joinToString { it.contentType })
    addStatement("setHeader(%S, 415)", STATUS)
    addStatement("body = null")
    addStatement("return this")
    endControlFlow()

    endControlFlow()
}

private fun FunSpec.Builder.callMethod(
    entryPoint: Script,
) {
    val hasOutput = entryPoint.outputJvmName != "kotlin.Unit"

    if (entryPoint.isSuspend) {
        addStatement(
            "val executor = %T.newCachedThreadPool()",
            ClassName("java.util.concurrent", "Executors"),
        )
        addStatement(
            "val executorCoroutineDispatcher = executor.%M()",
            MemberName("kotlinx.coroutines", "asCoroutineDispatcher", isExtension = true),
        )

        if (hasOutput) {
            beginControlFlow("val output = try")
        } else {
            beginControlFlow("try")
        }
        addStatement(
            "val scope = %M(executorCoroutineDispatcher)",
            MemberName("kotlinx.coroutines", "CoroutineScope", isExtension = true),
        )
        beginControlFlow(
            "val deferred = scope.%M {",
            MemberName("kotlinx.coroutines", "async", isExtension = true),
        )
        addStatement(
            "%M(%L)%L",
            MemberName(entryPoint.packageName, entryPoint.name, isExtension = true),
            entryPoint.getInputs(),
            if (entryPoint.outputIsNullable) {
                CodeBlock.of(" ?: return this")
            } else {
                CodeBlock.of("")
            },
        )
        endControlFlow()

        addStatement(
            "deferred.%M().get()",
            MemberName("kotlinx.coroutines.future", "asCompletableFuture"),
        )

        nextControlFlow(
            "catch (suspendUserError: %T)",
            ClassName("java.util.concurrent", "ExecutionException"),
        )
        addStatement("throw suspendUserError.cause!!")
        nextControlFlow("finally")
        addStatement("executorCoroutineDispatcher.close()")
        endControlFlow()
    } else {
        if (hasOutput) {
            addStatement(
                "val output = %M(%L)%L",
                MemberName(entryPoint.packageName, entryPoint.name, isExtension = true),
                entryPoint.getInputs(),
                if (entryPoint.outputIsNullable) {
                    CodeBlock.of(" ?: return this")
                } else {
                    CodeBlock.of("")
                },
            )
        } else {
            addStatement(
                "%M(%L)%L",
                MemberName(entryPoint.packageName, entryPoint.name, isExtension = true),
                entryPoint.getInputs(),
                if (entryPoint.outputIsNullable) {
                    CodeBlock.of(" ?: return this")
                } else {
                    CodeBlock.of("")
                },
            )
        }
    }
}

private val builtInSerializer = MemberName("kotlinx.serialization.builtins", "serializer")

internal fun CodeGenTree.Type.getSerializer(): CodeBlock = if (hasStar()) {
    error("Star types are not allowed")
} else {
    getSerializerOrFail()
}

private fun CodeGenTree.Type.hasStar(): Boolean = when (this) {
    is CodeGenTree.Enum -> false
    is NormalClass -> types.any { it.hasStar() }
    is Type.ARRAY -> item.hasStar()
    is Type.LIST -> item.hasStar()
    is Type.SET -> item.hasStar()
    is Type.Builtin -> false
    is Type.MAP -> key.hasStar() || value.hasStar()
    is Type.Parameter -> false
    Type.STAR -> true
    is Type.Unknown -> error("Not possible $this")
}

private fun CodeGenTree.Type.getSerializerOrFail(): CodeBlock = when (this) {
    is CodeGenTree.Enum -> CodeBlock.of("%T.serializer()", ClassName(packageName, names))

    is NormalClass -> CodeBlock.of(
        "%T.serializer(%L)",
        ClassName(packageName, names),
        if (types.isEmpty()) {
            CodeBlock.of("")
        } else {
            val s = CodeBlock.builder()
            for (type in types) {
                s.add("%L, ", type.getSerializer())
            }
            s.build()
        },
    )

    is Type.ARRAY -> CodeBlock.of(
        "%M(%L)",
        MemberName("kotlinx.serialization.builtins", "ArraySerializer"),
        item.getSerializer(),
    )

    Type.Builtin.BOOLEAN -> CodeBlock.of("%T.%M()", BOOLEAN, builtInSerializer)

    Type.Builtin.BYTEARRAY -> CodeBlock.of(
        "%M()",
        MemberName("kotlinx.serialization.builtins", "ByteArraySerializer"),
    )

    Type.Builtin.BYTESTRING -> CodeBlock.of("%T.%M()", STRING, builtInSerializer)

    Type.Builtin.CHARARRAY -> CodeBlock.of(
        "%M()",
        MemberName("kotlinx.serialization.builtins", "CharArraySerializer"),
    )

    Type.Builtin.DOUBLE -> CodeBlock.of("%T.%M()", DOUBLE, builtInSerializer)

    Type.Builtin.DURATION -> CodeBlock.of("%T.%M()", ClassName("kotlin.time", "Duration"), builtInSerializer)

    Type.Builtin.FILE -> error("Not supported")

    is Type.Unknown -> error("Not possible")

    Type.Builtin.FLOAT -> CodeBlock.of("%T.%M()", FLOAT, builtInSerializer)

    Type.Builtin.INT -> CodeBlock.of("%T.%M()", INT, builtInSerializer)

    Type.Builtin.LONG -> CodeBlock.of("%T.%M()", LONG, builtInSerializer)

    Type.Builtin.STRING -> CodeBlock.of("%T.%M()", STRING, builtInSerializer)

    Type.Builtin.UNIT -> CodeBlock.of("%T.%M()", UNIT, builtInSerializer)

    Type.Builtin.UUID -> CodeBlock.of("%T.%M()", ClassName("kotlin.uuid", "Uuid"), builtInSerializer)

    Type.DateType.DATE -> CodeBlock.of("%T.serializer()", ClassName("kotlinx.datetime", "LocalDate"))

    Type.DateType.INSTANT -> CodeBlock.of("%T.serializer()", ClassName("kotlin.time", "Instant"))

    is Type.LIST -> CodeBlock.of(
        "%M(%L)",
        MemberName("kotlinx.serialization.builtins", "ListSerializer"),
        item.getSerializer(),
    )

    is Type.SET -> CodeBlock.of(
        "%M(%L)",
        MemberName("kotlinx.serialization.builtins", "SetSerializer"),
        item.getSerializer(),
    )

    is Type.MAP -> CodeBlock.of(
        "%M(%L, %L)",
        MemberName("kotlinx.serialization.builtins", "MapSerializer"),
        key.getSerializer(),
        value.getSerializer(),
    )

    is Type.Parameter -> error("Not supported Type.Parameter with name $name")

    is Type.STAR -> error("Not supported Type.STAR")

    Type.Builtin.BYTE -> CodeBlock.of("%T.%M()", BYTE, builtInSerializer)

    Type.Builtin.CHAR -> CodeBlock.of("%T.%M()", CHAR, builtInSerializer)

    Type.Builtin.SHORT -> CodeBlock.of("%T.%M()", SHORT, builtInSerializer)
}

private fun Script.Body.getSerializer(): CodeBlock {
    val serializer = klass.getSerializer()

    return if (nullable) {
        CodeBlock.of("%L.%M", serializer, MemberName("kotlinx.serialization.builtins", "nullable"))
    } else {
        serializer
    }
}

private fun Script.getInputs(): CodeBlock = CodeBlock.builder().apply {
    for (input in inputs) {
        when (input) {
            is Script.Body -> {
                when {
                    input.klass.isInputStream() -> {
                        add(
                            "${input.propertyName} = getBody(%T::class.java),\n",
                            INPUT_STREAM,
                        )
                    }

                    input.klass.isSource() -> {
                        add(
                            "${input.propertyName} = getBody(%T::class.java).%M().%M(),\n",
                            INPUT_STREAM,
                            MemberName("kotlinx.io", "asSource", isExtension = true),
                            MemberName("kotlinx.io", "buffered", isExtension = true),
                        )
                    }

                    input.klass.isRawSource() -> {
                        add(
                            "${input.propertyName} = getBody(%T::class.java).%M().%M(),\n",
                            INPUT_STREAM,
                            MemberName("kotlinx.io", "asSource", isExtension = true),
                        )
                    }

                    input.contentNegotiations.isNotEmpty() -> {
                        add(
                            "${input.propertyName} = requestFactory.decodeFromString(%L, getBody(%T::class.java)),\n",
                            input.getSerializer(),
                            STRING,
                        )
                    }

                    else -> {
                        add(
                            "${input.propertyName} = body as %T,\n",
                            input.klass.toPoetType(includeGenerics = true).copy(nullable = input.nullable),
                        )
                    }
                }
            }

            is Script.Header -> add(
                "${input.propertyName} = getHeader(%S, %T::class.java),\n",
                input.name,
                input.klass.toPoetType(),
            )

            is Script.None -> when {
                input.klass.isMessage() -> add("${input.propertyName} = this@$name,\n")

                input.klass.isMessageLog() -> add("${input.propertyName} = messageLog,\n")

                input.klass.isKeyManager() -> add(
                    "${input.propertyName} = %T.getService<%T>(%T::class.java, null).keyManager,\n",
                    ITApiFactory,
                    KeystoreService,
                    KeystoreService,
                )

                !input.hasDefault -> add(
                    "${input.propertyName} = getProperty(%S) as %T,\n",
                    input.propertyName,
                    input.klass.toPoetType(includeGenerics = true).copy(nullable = input.nullable),
                )
            }

            is Script.Password -> add(
                "${input.propertyName} = %T.getService<%T>(%T::class.java, null).getUserCredential(getProperty(%S) as %T).password,\n",
                ITApiFactory,
                SecureStoreService,
                SecureStoreService,
                input.propertyName,
                STRING,
            )

            is Script.Property if input.klass.isDataSource() -> add(
                "${input.propertyName} = %T.doLookup<%T>(%P),\n",
                ClassName("javax.naming", "InitialContext"),
                DATA_SOURCE,
                CodeBlock.of(
                    "osgi:service/javax.sql.DataSource/(osgi.jndi.service.name=\${getProperty(%S) as %T})",
                    input.name,
                    STRING,
                ),
            )

            is Script.Property -> add(
                "${input.propertyName} = getProperty(%S) as %T,\n",
                input.name,
                input.klass.toPoetType(includeGenerics = true).copy(nullable = input.nullable),
            )

            is Script.Parameter -> {
                val klass = input.klass
                add(
                    "${input.propertyName} = (getProperty(%S) as %T)%L%L,\n",
                    input.propertyName,
                    STRING.copy(nullable = input.nullable),
                    if (klass == Type.Builtin.STRING || !input.nullable) {
                        CodeBlock.of("")
                    } else {
                        CodeBlock.of("?")
                    },
                    CodeBlock.builder().apply {
                        when (klass) {
                            Type.Builtin.BOOLEAN -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toBoolean", isExtension = true),
                            )

                            Type.Builtin.BYTEARRAY -> add(
                                ".%M()",
                                MemberName("kotlin.text", "encodeToByteArray", isExtension = true),
                            )

                            Type.Builtin.BYTESTRING -> Unit

                            Type.Builtin.CHARARRAY -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toCharArray", isExtension = true),
                            )

                            Type.Builtin.DOUBLE -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toDouble", isExtension = true),
                            )

                            Type.Builtin.DURATION -> TODO()

                            Type.Builtin.FILE -> TODO()

                            Type.Builtin.FLOAT -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toFloat", isExtension = true),
                            )

                            Type.Builtin.INT -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toInt", isExtension = true),
                            )

                            Type.Builtin.LONG -> add(
                                ".%M()",
                                MemberName("kotlin.text", "toLong", isExtension = true),
                            )

                            Type.Builtin.STRING -> Unit

                            Type.Builtin.UNIT -> TODO()

                            Type.Builtin.UUID -> TODO()

                            Type.DateType.DATE -> TODO()

                            Type.DateType.INSTANT -> TODO()

                            Type.Builtin.BYTE -> TODO()

                            Type.Builtin.CHAR -> TODO()

                            Type.Builtin.SHORT -> TODO()
                        }
                    }.build(),
                )
            }
        }
    }
}.build()

private fun FunSpec.Builder.addOutputs(
    outputs: Collection<Script.Output>,
    ref: String,
    isError: Boolean,
) {
    if (!isError) {
        addStatement("""setProperty(%S, $ref)""", "_RESULT_")
    }
    for (output in outputs) {
        when (output) {
            is Script.Body -> {
                when {
                    output.klass.isNothing() -> {
                        addStatement("body = null")
                    }

                    output.klass.isInputStream() || output.klass.isByteArray() -> {
                        addStatement("body = $ref.${output.propertyName}")
                    }

                    output.klass.isSource() -> {
                        addStatement(
                            "body = $ref.${output.propertyName}.%M()",
                            MemberName("kotlinx.io", "asInputStream", isExtension = true),
                        )
                    }

                    output.contentNegotiations.isNotEmpty() -> {
                        if (isError) {
                            addStatement(
                                "body = errorFactory.encodeToString(%L, $ref.${output.propertyName})",
                                output.getSerializer(),
                            )
                        } else {
                            addStatement(
                                "body = responseFactory.encodeToString(%L, $ref.${output.propertyName})",
                                output.getSerializer(),
                            )
                        }
                    }

                    else -> {
                        addStatement("body = $ref.${output.propertyName}")
                    }
                }
                if (isError) {
                    addStatement(
                        """messageLog.addAttachmentAsString(%S, %L.toString(), "text/plain")""",
                        ref,
                        ref,
                    )
                }

                if (isError) {
                    addStatement("""setHeader("Content-Type", errorContentType)""")
                } else if (output.contentNegotiations.isNotEmpty()) {
                    addStatement("""setHeader("Content-Type", responseContentType)""")
                }
            }

            is Script.Property -> addStatement(
                """setProperty(%S, $ref.${output.propertyName})""",
                output.name,
            )

            is Script.Header -> addStatement(
                """setHeader(%S, $ref.${output.propertyName})""",
                output.name,
            )

            is Script.DynamicHeaders -> {
                beginControlFlow("""for (header in $ref.${output.propertyName})""")
                addStatement("""setHeader(header.key, header.value)""")
                endControlFlow()
            }
        }
    }
}

internal val MESSAGE = ClassName("com.sap.gateway.ip.core.customdev.util", "Message")

internal fun Type.isMessage(): Boolean = this is NormalClass && qualifiedName == MESSAGE.canonicalName

internal val MESSAGE_LOG = ClassName("com.sap.it.api.msglog", "MessageLog")

internal fun Type.isMessageLog(): Boolean = this is NormalClass && qualifiedName == MESSAGE_LOG.canonicalName

internal val DATA_SOURCE = ClassName("javax.sql", "DataSource")

internal fun Type.isDataSource(): Boolean = this is NormalClass && qualifiedName == DATA_SOURCE.canonicalName

internal val KEY_MANAGER = ClassName("javax.net.ssl", "KeyManager")

internal fun Type.isKeyManager(): Boolean = this is NormalClass && qualifiedName == KEY_MANAGER.canonicalName

internal val INPUT_STREAM = ClassName("java.io", "InputStream")

internal fun Type.isInputStream(): Boolean = this is NormalClass && qualifiedName == INPUT_STREAM.canonicalName

internal fun Type.isByteArray(): Boolean = this is NormalClass && qualifiedName == BYTE_ARRAY.canonicalName

private val SOURCE = ClassName("kotlinx.io", "Source")
private val RAW_SOURCE = ClassName("kotlinx.io", "RawSource")

internal fun Type.isSource(): Boolean = this is NormalClass && qualifiedName == SOURCE.canonicalName

internal fun Type.isRawSource(): Boolean = this is NormalClass && qualifiedName == RAW_SOURCE.canonicalName

internal fun Type.isNothing(): Boolean =
    this is NormalClass && qualifiedName == NOTHING.canonicalName

private val SecureStoreService = ClassName("com.sap.it.api.securestore", "SecureStoreService")
private val KeystoreService = ClassName("com.sap.it.api.keystore", "KeystoreService")
private val ITApiFactory = ClassName("com.sap.it.api", "ITApiFactory")
private val STRING_FORMAT = ClassName("kotlinx.serialization", "StringFormat")
