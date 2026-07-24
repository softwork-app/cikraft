package app.softwork.cikraft.generator

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.core.Script
import app.softwork.cikraft.core.Script.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.hfhbd.kfx.codegen.CodeGenTree.*
import io.github.hfhbd.kfx.kotlin.*

public fun generateKtorServer(
    createdFlow: CreatedFlow,
): FileSpec? = when (createdFlow.sender) {
    is CreatedFlow.Sender.Https -> generateKtorServerRoute(createdFlow)
    is CreatedFlow.Sender.DataStore -> generateDataStorePolling(createdFlow)
    null -> null
}

private fun generateDataStorePolling(createdFlow: CreatedFlow): FileSpec {
    val packageName = createdFlow.packageName.toPackageName()
    val file = FileSpec.builder(packageName, createdFlow.rawId)
    val configObject = ClassName(packageName, createdFlow.rawId + "Config")

    val function = FunSpec.builder(createdFlow.rawId)
    function.addModifiers(KModifier.SUSPEND)
    function.receiver(
        LambdaTypeName.get(
            parameters = listOf(ParameterSpec.unnamed(STRING)),
            returnType = ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(STRING),
        ),
    )

    function.addParameters(createdFlow, configObject)
    val sender = createdFlow.sender as CreatedFlow.Sender.DataStore

    function.beginControlFlow("invoke(%S).collect", sender.name)
    function.addStatement(
        "%M(%L.%M)",
        MemberName("kotlinx.coroutines", "delay", isExtension = true),
        sender.pollDelay.inWholeSeconds,
        ClassName("kotlin.time", "Duration").nestedClass("Companion").member("seconds"),
    )

    function.callFunctionFromDataStore(createdFlow)
    function.endControlFlow()

    file.addFunction(function.build())

    return file.build()
}

private fun generateKtorServerRoute(createdFlow: CreatedFlow): FileSpec {
    val packageName = createdFlow.packageName.toPackageName()
    val resourcesClassName = ClassName(createdFlow.packageName.toPackageName(), createdFlow.rawId)

    val file = FileSpec.builder(packageName, createdFlow.rawId)

    val configObject = ClassName(packageName, createdFlow.rawId + "Config")

    val function = FunSpec.builder(createdFlow.rawId)
    function.receiver(ClassName("io.ktor.server.routing", "Route"))

    function.addParameters(createdFlow, configObject)

    val sender = createdFlow.sender as CreatedFlow.Sender.Https
    if (sender.csrfProtection) {
        function.addCSRFVariables(createdFlow.rawId)

        function.addCSRFHead(resourcesClassName)
        function.endControlFlow()

        function.beginControlFlow(
            "%M(%S, csrfToken)",
            MemberName("io.ktor.server.routing", "header", isExtension = true),
            "X-CSRF-Token",
        )
    }

    val contentType = function.addContentType(createdFlow.scripts)
    val accept = function.addAccept(createdFlow.scripts)

    function.beginControlFlow(
        "%M<%T>",
        MemberName("io.ktor.server.resources", "post", isExtension = true),
        resourcesClassName,
    )
    if (sender.csrfProtection) {
        function.addCSRFCheck()
    }

    function.addStatement(
        "call.response.%M(%M, %T.random().toString())",
        header,
        MESSAGE_LOG_HEADER,
        ClassName("kotlin.uuid", "Uuid"),
    )
    function.returnResultClass(createdFlow)
    if (accept) {
        function.endControlFlow()
    }
    if (contentType) {
        function.endControlFlow()
    }
    if (sender.csrfProtection) {
        function.endControlFlow()
    }
    function.endControlFlow()

    file.addFunction(function.build())

    return file.build()
}

private fun FunSpec.Builder.addContentType(
    entryPoints: List<Script>,
): Boolean {
    val firstInputBody = entryPoints.firstOrNull()?.bodyInput
    return if (firstInputBody != null) {
        beginControlFlow(
            "%M(%L)",
            MemberName("app.softwork.cikraft.ktor.server.runtime", "contentType", isExtension = true),
            firstInputBody.contentNegotiations.map {
                it.contentTypeKtor(includeParams = false)
            }.joinToCode(),
        )
        true
    } else {
        false
    }
}

private fun FunSpec.Builder.addAccept(
    entryPoints: List<Script>,
): Boolean {
    val lastEntrypoint = entryPoints.lastOrNull()
    val lastOutputBody = lastEntrypoint?.bodyOutput
    return if (lastOutputBody != null) {
        beginControlFlow(
            "%M(%L)",
            MemberName("io.ktor.server.routing", "accept", isExtension = true),
            lastOutputBody.contentNegotiations.map {
                it.contentTypeKtor(includeParams = false)
            }.joinToCode(),
        )
        true
    } else {
        false
    }
}

private fun FunSpec.Builder.addCSRFVariables(
    rawId: String,
) {
    addStatement(
        "val csrfToken = %S + %T.random()",
        "csrfToken$rawId",
        ClassName("kotlin.uuid", "Uuid"),
    )
    addStatement(
        "val csrfServerSessionCookie = %S + %T.random()",
        "csrfSessionCookie$rawId",
        ClassName("kotlin.uuid", "Uuid"),
    )
    addStatement(
        "val csrfServerVCAPCookie = %S + %T.random()",
        "csrfVCAPCookie$rawId",
        ClassName("kotlin.uuid", "Uuid"),
    )

    beginControlFlow(
        "%M(%S, %S)",
        MemberName("io.ktor.server.routing", "header", isExtension = true),
        "X-CSRF-Token",
        "FETCH",
    )
}

private fun FunSpec.Builder.addCSRFHead(
    resourcesClassName: ClassName,
) {
    beginControlFlow(
        "%M<%T>",
        MemberName("io.ktor.server.resources", "head", isExtension = true),
        resourcesClassName,
    )

    addStatement("""call.response.%M("X-CSRF-Token", csrfToken)""", header)
    addStatement(
        """call.response.cookies.append(%T(name = "JSESSIONID", value = csrfServerSessionCookie))""",
        COOKIE,
    )
    addStatement(
        """call.response.cookies.append(Cookie(name = "__VCAP_ID__", value = csrfServerVCAPCookie))""",
        COOKIE,
    )
    addStatement(
        "call.response.%M(%M, %T.random().toString())",
        header,
        MESSAGE_LOG_HEADER,
        ClassName("kotlin.uuid", "Uuid"),
    )

    addStatement(
        """call.%M(%M)""",
        MemberName("io.ktor.server.response", "respond", isExtension = true),
        HTTP_STATUS_CODE.nestedClass("Companion").member("OK"),
    )
    endControlFlow()
}

private fun FunSpec.Builder.addCSRFCheck() {
    addStatement(
        """val csrfRequestSessionCookie = call.request.cookies["JSESSIONID"]""",
    )
    addStatement(
        """val csrfRequestVCAPCookie = call.request.cookies["__VCAP_ID__"]""",
    )
    beginControlFlow(
        """if (csrfRequestSessionCookie != csrfServerSessionCookie || csrfRequestVCAPCookie != csrfServerVCAPCookie)""",
    )
    addStatement(
        """call.%M(%M)""",
        MemberName("io.ktor.server.response", "respond", isExtension = true),
        HTTP_STATUS_CODE.nestedClass("Companion").member("Forbidden"),
    )
    addStatement("""return@post""")
    endControlFlow()
}

private fun FunSpec.Builder.addParameters(
    createdFlow: CreatedFlow,
    configObject: ClassName,
) {
    for (input in createdFlow.scripts.flatMap { it.inputs }.distinctBy { it.propertyName }) {
        when (input) {
            is None -> {
                addParameter(
                    name = input.propertyName,
                    type = input.klass.toKotlinPoet(input.nullable),
                )
            }

            is Body -> continue

            is Header -> continue

            is Password,
            is Property,
            is Parameter,
            -> {
                if (input.klass.isDataSource()) {
                    addParameter(
                        name = input.propertyName,
                        type = input.klass.toKotlinPoet(input.nullable),
                    )
                } else if (input.klass.isKeyManager()) {
                    addParameter(
                        name = input.propertyName,
                        type = input.klass.toKotlinPoet(input.nullable),
                    )
                } else {
                    addParameter(
                        ParameterSpec.builder(
                            name = input.propertyName,
                            type = input.klass.toKotlinPoet(input.nullable),
                        ).apply {
                            if (input !is Password) {
                                defaultValue("%T.${input.propertyName}", configObject)
                            }
                        }.build(),
                    )
                }
            }
        }
    }
}

internal fun ClassName.convertFromString(nullable: Boolean): CodeBlock = when {
    this == STRING -> CodeBlock.of("")

    this == INT -> CodeBlock.of(if (nullable) "?.toInt()" else ".toInt()")

    this == DOUBLE -> CodeBlock.of(if (nullable) "?.toDouble()" else ".toDouble()")

    this == FLOAT -> CodeBlock.of(if (nullable) "?.toFloat()" else ".toFloat()")

    this == CHAR_ARRAY -> CodeBlock.of(
        if (nullable) "?.toCharArray()" else ".toCharArray()",
    )

    this == BOOLEAN -> CodeBlock.of(if (nullable) "?.toBooleanStrict()" else ".toBooleanStrict()")

    this == DATA_SOURCE -> CodeBlock.of("")

    else -> error("Not yet supported $this")
}

private fun FunSpec.Builder.getResponseFactory(
    output: Body,
    isError: Boolean,
) {
    if (isError) {
        beginControlFlow(
            "val (errorResponseFactory, errorContentType) = when",
        )
    } else {
        beginControlFlow(
            "val (responseFactory, responseContentType) = when",
        )
    }

    addStatement("acceptContentTypes.any { it == %M } ||", CONTENT_TYPE.nestedClass("Companion").member("Any"))
    for (contentNegotiation in output.contentNegotiations) {
        addStatement(
            "acceptContentTypes.any { %L.match(it) } -> %T%L to %S",
            contentNegotiation.contentTypeKtor(includeParams = true),
            contentNegotiation.factoryKlass.toKotlinPoet(false),
            CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
            contentNegotiation.contentType(),
        )
    }

    beginControlFlow("else ->")
    addStatement(
        "call.%M(%M)",
        MemberName("io.ktor.server.response", "respond", isExtension = true),
        HTTP_STATUS_CODE.nestedClass("Companion").member("NotAcceptable"),
    )
    addStatement("return@post")
    endControlFlow()

    endControlFlow()
}

private fun FunSpec.Builder.getRequestFactory(
    firstBody: Body,
) {
    addStatement(
        "val requestContentType = call.request.%M()",
        MemberName("io.ktor.server.request", "contentType", isExtension = true),
    )
    beginControlFlow(
        "val requestFactory = when",
    )

    addStatement("requestContentType.match(%M) ||", CONTENT_TYPE.nestedClass("Companion").member("Any"))
    for (contentNegotiation in firstBody.contentNegotiations) {
        addStatement(
            "%L.match(requestContentType) -> %T%L",
            contentNegotiation.contentTypeKtor(includeParams = true),
            contentNegotiation.factoryKlass.toKotlinPoet(false),
            CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
        )
    }

    beginControlFlow("else ->")
    addStatement(
        "call.response.%M(%S, %S)",
        header,
        "Accept-Post",
        firstBody.contentNegotiations.joinToString { it.contentType },
    )
    addStatement(
        "call.%M(%M)",
        MemberName("io.ktor.server.response", "respond", isExtension = true),
        HTTP_STATUS_CODE.nestedClass("Companion").member("UnsupportedMediaType"),
    )
    addStatement("return@post")
    endControlFlow()

    endControlFlow()
}

private fun FunSpec.Builder.returnResultClass(createdFlow: CreatedFlow) {
    val lastEntrypoint = createdFlow.scripts.lastOrNull {
        it.bodyOutput != null && it.bodyOutput!!.contentNegotiations.isNotEmpty()
    }

    val lastOutputBody = lastEntrypoint?.bodyOutput

    val error = createdFlow.scripts.firstOrNull {
        it.error != null
    }?.error

    if (lastOutputBody != null || error != null) {
        addStatement(
            "val acceptContentTypes = call.request.%M()?.let { it.split(%S).map { %T.parse(it.trim()) }} ?: listOf(%M)",
            MemberName("io.ktor.server.request", "accept", isExtension = true),
            ",",
            CONTENT_TYPE,
            CONTENT_TYPE.nestedClass("Companion").member("Any"),
        )
    }

    if (lastOutputBody != null) {
        getResponseFactory(lastOutputBody, isError = false)
    }
    if (error != null) {
        getResponseFactory(error.bodyOutput, isError = true)
    }

    val firstEntrypoint = createdFlow.scripts.firstOrNull {
        it.bodyInput != null
    }

    val firstBody = firstEntrypoint?.bodyInput?.takeUnless {
        it.contentNegotiations.isEmpty()
    }

    if (firstBody != null) {
        getRequestFactory(firstBody)
    }

    if (error != null) {
        beginControlFlow("try")
    }

    callFunction(createdFlow)

    for (entryPoint in createdFlow.scripts) {
        for (output in entryPoint.outputs) {
            addOutput(output, "result")
        }
    }

    val lastStatus = lastEntrypoint?.outputs?.singleOrNull {
        it is Header && it.name == STATUS
    } as Header?

    when {
        lastOutputBody != null -> {
            respondBody("result", lastOutputBody, isError = false)
        }

        lastStatus != null -> {
            respondStatus(lastStatus)
        }

        else -> addStatement(
            """call.%M(%M)""",
            MemberName("io.ktor.server.response", "respond", isExtension = true),
            HTTP_STATUS_CODE.nestedClass("Companion").member("NoContent"),
        )
    }
    if (error != null) {
        nextControlFlow(
            "catch (exception: %T)",
            ClassName(
                error.packageName,
                error.name,
            ),
        )
        for (output in error.outputs) {
            addOutput(output, "exception")
        }

        respondBody("exception", error.bodyOutput, isError = true)

        endControlFlow()
    }
}

private fun FunSpec.Builder.addOutput(
    output: Output,
    ref: String,
) {
    when (output) {
        is DynamicHeaders -> {
            beginControlFlow("for ((key, value) in $ref.${output.propertyName})")
            addStatement(
                "call.response.%M(key, value)",
                header,
            )
            endControlFlow()
        }

        is Header -> {
            if (output.name == STATUS) {
                setStatus(ref, output)
            } else {
                if (output.nullable) {
                    beginControlFlow("if ($ref.${output.propertyName} != null)")
                }
                addStatement(
                    "call.response.%M(%S, $ref.${output.propertyName})",
                    header,
                    output.name,
                )
                if (output.nullable) {
                    endControlFlow()
                }
            }
        }

        is Property,
        is Body,
        -> return
    }
}

private fun FunSpec.Builder.callFunction(createdFlow: CreatedFlow) {
    addStatement(
        "val result = %M(%L)",
        MemberName(createdFlow.packageName.toPackageName(), createdFlow.rawId + "Function", isExtension = true),
        CodeBlock.builder().apply {
            for (input in createdFlow.scripts.flatMap { it.inputs }.distinctBy { it.propertyName }) {
                when (input) {
                    is Body -> {
                        val usesContentNegotiation = input.contentNegotiations.isNotEmpty()
                        if (input.klass.isSource() || input.klass.isRawSource() || input.klass.isInputStream()) {
                            add(
                                "${input.propertyName} = call.%M(),",
                                MemberName("io.ktor.server.request", "receive", isExtension = true),
                            )
                        } else if (usesContentNegotiation) {
                            add(
                                "${input.propertyName} = requestFactory.decodeFromString(%L, call.%M()),",
                                input.klass.getSerializer(),
                                MemberName("io.ktor.server.request", "receiveText", isExtension = true),
                            )
                        }
                    }

                    is Header -> add(
                        "${input.propertyName} = call.request.%M(%S)%L%L,",
                        MemberName("io.ktor.server.request", "header", isExtension = true),
                        input.name,
                        input.klass.toPoetType().convertFromString(input.nullable),
                        if (input.nullable) {
                            CodeBlock.of("")
                        } else {
                            CodeBlock.of("!!")
                        },
                    )

                    is None,
                    is Password,
                    is Property,
                    is Parameter,
                    -> add("${input.propertyName} = ${input.propertyName},")
                }
            }
        }.build(),
    )
}
private fun FunSpec.Builder.callFunctionFromDataStore(createdFlow: CreatedFlow) {
    addStatement(
        "val result = %M(%L)",
        MemberName(createdFlow.packageName.toPackageName(), createdFlow.rawId + "Function", isExtension = true),
        CodeBlock.builder().apply {
            for (input in createdFlow.scripts.flatMap { it.inputs }.distinctBy { it.propertyName }) {
                when (input) {
                    is Body -> {
                        val usesContentNegotiation = input.contentNegotiations.isNotEmpty()
                        if (usesContentNegotiation) {
                            val contentNegotiation = input.contentNegotiations.first()
                            add(
                                "${input.propertyName} = %T%L.decodeFromString(%L, it),",
                                contentNegotiation.factoryKlass.toKotlinPoet(false),
                                CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
                                input.klass.getSerializer(),
                            )
                        } else {
                            add("${input.propertyName} = it,")
                        }
                    }

                    is Header -> TODO("Headers are not yet supported")

                    is None,
                    is Password,
                    is Property,
                    is Parameter,
                    -> add("${input.propertyName} = ${input.propertyName},")
                }
            }
        }.build(),
    )
}

private fun FunSpec.Builder.respondBody(
    ref: String,
    outputBody: Body,
    isError: Boolean,
) {
    if (isError) {
        addStatement(
            "call.response.%M(name = %M, value = errorContentType)",
            MemberName("io.ktor.server.response", "header", isExtension = true),
            MemberName(ClassName("io.ktor.http", "HttpHeaders"), "ContentType"),
        )
    } else {
        addStatement(
            "call.response.%M(name = %M, value = responseContentType)",
            MemberName("io.ktor.server.response", "header", isExtension = true),
            MemberName(ClassName("io.ktor.http", "HttpHeaders"), "ContentType"),
        )
    }

    if (outputBody.klass.isSource() || outputBody.klass.isInputStream()) {
        addStatement(
            "call.%M($ref.${outputBody.propertyName})",
            MemberName("io.ktor.server.response", "respond"),
        )
    } else if (isError) {
        addStatement(
            "call.%M(text = errorResponseFactory.encodeToString(%L, $ref.${outputBody.propertyName}))",
            MemberName("io.ktor.server.response", "respondText"),
            outputBody.klass.getSerializer(),
        )
    } else {
        addStatement(
            "call.%M(text = responseFactory.encodeToString(%L, $ref.${outputBody.propertyName}))",
            MemberName("io.ktor.server.response", "respondText"),
            outputBody.klass.getSerializer(),
        )
    }
}

private fun FunSpec.Builder.respondStatus(
    output: Header,
) = addStatement(
    "call.%M(%T.fromValue(result.${output.propertyName}))",
    MemberName("io.ktor.server.response", "respond", isExtension = true),
    HTTP_STATUS_CODE,
)

private fun FunSpec.Builder.setStatus(
    ref: String,
    output: Header,
) {
    addStatement(
        "call.response.status(%T.fromValue($ref.${output.propertyName}))",
        HTTP_STATUS_CODE,
    )
}

private val header = MemberName("io.ktor.server.response", "header", isExtension = true)
internal const val STATUS = "CamelHttpResponseCode"
private val COOKIE = ClassName("io.ktor.http", "Cookie")
private val CONTENT_TYPE = ClassName("io.ktor.http", "ContentType")
private val HTTP_STATUS_CODE = ClassName("io.ktor.http", "HttpStatusCode")

internal fun Type.toKotlinPoet(nullable: Boolean): TypeName = toPoetType().copy(nullable = nullable)

private val MESSAGE_LOG_HEADER = MemberName(
    "app.softwork.cikraft",
    "SAP_MESSAGE_PROCESSING_LOG_ID_HEADER",
    isExtension = true,
)

internal fun Body.ContentNegotiation.contentTypeKtor(includeParams: Boolean): CodeBlock {
    val contentType = when (contentType) {
        "application/json" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("Json"))
        "application/problem+json" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("ProblemJson"))
        "application/problem+xml" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("ProblemXml"))
        "application/xml" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("Xml"))
        "application/soap+xml" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("Soap"))
        "text/csv" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Text").member("CSV"))
        "text/xml" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Text").member("Xml"))
        "application/octet-stream" -> CodeBlock.of("%M", CONTENT_TYPE.nestedClass("Application").member("OctetStream"))
        else -> CodeBlock.of("%T.parse(%S)", CONTENT_TYPE, contentType)
    }

    return if (includeParams) {
        val parameters = parameters.map { parameter ->
            CodeBlock.of("withParameter(%S, %S)", parameter.key, parameter.value)
        }
        (listOf(contentType) + parameters).joinToCode(".")
    } else {
        contentType
    }
}
