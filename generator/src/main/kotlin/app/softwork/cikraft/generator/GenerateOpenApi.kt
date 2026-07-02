package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*
import app.softwork.cikraft.core.Script.*
import io.github.hfhbd.kfx.codegen.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.Type.*
import io.github.hfhbd.kfx.openapi.model.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.Components.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.Components.Schema.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.Components.Schema.ARRAY
import io.github.hfhbd.kfx.openapi.model.OpenApi.Components.Schema.OBJECT.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.Operation
import io.github.hfhbd.kfx.openapi.model.OpenApi.Operation.*
import io.github.hfhbd.kfx.openapi.model.OpenApi.Operation.Header
import kotlinx.serialization.json.JsonPrimitive

public fun generateOpenApi(
    infrastructure: OpenApiInfrastructure,
    transformers: List<SAPOpenAPITransformer> = emptyList(),
): OpenApi {
    val components = buildMap {
        for (script in infrastructure.apis.flatMap { it.scripts }) {
            val bodyInput = script.bodyInput
            if (bodyInput != null && bodyInput.contentNegotiations.isNotEmpty()) {
                val isNotOctetStream =
                    bodyInput.contentNegotiations.singleOrNull()?.contentType != "application/octet-stream"
                if (isNotOctetStream) {
                    addType(bodyInput.klass, bodyInput.sealedSubClasses)
                    for (sealedSubclass in bodyInput.sealedSubClasses) {
                        addType(sealedSubclass.klass, sealedSubclass.parent.toRef())
                    }
                }
            }
            val bodyOutput = script.bodyOutput
            if (bodyOutput != null && bodyOutput.contentNegotiations.isNotEmpty()) {
                val isNotOctetStream =
                    bodyOutput.contentNegotiations.singleOrNull()?.contentType != "application/octet-stream"
                if (isNotOctetStream) {
                    addType(bodyOutput.klass, bodyOutput.sealedSubClasses)
                    for (sealedSubclass in bodyOutput.sealedSubClasses) {
                        addType(sealedSubclass.klass, sealedSubclass.parent.toRef())
                    }
                }
            }
            val errorBody = script.error?.bodyOutput
            if (errorBody != null) {
                addType(errorBody.klass, errorBody.sealedSubClasses)
                for (sealedSubclass in errorBody.sealedSubClasses) {
                    addType(sealedSubclass.klass, sealedSubclass.parent.toRef())
                }
            }
        }
    }
    val httpApis = infrastructure.apis.filter {
        when (it.sender) {
            is CreatedFlow.Sender.Https -> true
            is CreatedFlow.Sender.DataStore -> false
            null -> false
        }
    }
    val paths = paths(httpApis).toSortedMap(String::compareTo)
    var base = OpenApi(
        version = "3.1.0",
        info = Info(
            title = infrastructure.name,
            description = infrastructure.description,
            version = infrastructure.version,
        ),
        tags = infrastructure.tags.map {
            Tag(it.key, it.value)
        }.sortedBy {
            it.name
        },
        servers = infrastructure.servers.map {
            Server(it.key, description = it.value)
        }.sortedBy { it.url },
        paths = paths,
        components = Components(
            schemas = components.toSortedMap(String::compareTo),
            securitySchemes = httpApis.associate {
                val sender = it.sender as CreatedFlow.Sender.Https
                val userRole = sender.role
                userRole to SecurityScheme.MutualTLS(description = "Client Cert connected to the User Role $userRole")
            }.toSortedMap(String::compareTo),
        ),
    )
    for (transformer in transformers) {
        base = transformer.convert(base, infrastructure)
    }
    return base
}

private fun paths(
    httpApis: List<CreatedFlow>,
): Map<String, Path> = httpApis.associate { api ->
    val sender = api.sender as CreatedFlow.Sender.Https

    var firstBody: Body? = null
    for (entryPoint in api.scripts) {
        val body = entryPoint.bodyInput
        if (body != null) {
            firstBody = body
            break
        }
    }

    val lastScript = api.scripts.lastOrNull()
    val headers: List<Script.Header> = api.scripts.flatMap {
        it.inputs.mapNotNull {
            when (it) {
                is Script.Header -> it
                else -> null
            }
        }
    }.distinctBy { it.name }

    val headersOutput = lastScript?.outputs?.mapNotNull {
        when (it) {
            is Body -> null

            is Script.DynamicHeaders -> null

            is Script.Header -> {
                if (it.name == "CamelHttpResponseCode") {
                    null
                } else {
                    it.name to Header(
                        schema = STRING(nullable = false),
                    )
                }
            }

            is Script.Property -> null
        }
    }?.toMap() ?: emptyMap()

    val path = Path(
        head = if (sender.csrfProtection) {
            csrfOperation(
                api = api,
                sender = sender,
                errorBody = lastScript?.error?.bodyOutput,
            )
        } else {
            null
        },
        post = iFlowOperation(
            api = api,
            sender = sender,
            bodyInput = firstBody,
            headersInput = headers,
            bodyOutput = lastScript?.bodyOutput,
            headersOutput = headersOutput,
            errorBody = lastScript?.error?.bodyOutput,
        ),
    )
    sender.url to path
}

private fun csrfOperation(
    api: CreatedFlow,
    sender: CreatedFlow.Sender.Https,
    errorBody: Body?,
): Operation = Operation(
    summary = "Get the CSRF Token for ${api.rawId}",
    id = api.rawId + "_CsrfToken",
    requestBody = null,
    tags = listOf(api.packageName),
    security = listOf(mapOf(sender.role to emptyList())),
    parameters = listOf(
        OpenApi.Parameter(
            name = "X-CSRF-Token",
            position = OpenApi.Parameter.Position.Header,
            required = true,
            schema = STRING(
                default = "FETCH",
            ),
        ),
    ),
    responses = mapOf(
        "200" to Response(
            description = "The CSRF Token for ${api.rawId}",
            headers = mapOf(
                "X-CSRF-Token" to Header(
                    schema = STRING(
                        nullable = false,
                    ),
                    required = true,
                ),
            ),
        ),
        "4XX" to Response(
            description = "Unexpected Error",
            content = errorBody?.contentNegotiations?.associate {
                it.contentType() to MediaType(
                    errorBody.klass.toSchema(
                        kdoc = null,
                        nullable = false,
                        refOnly = true,
                        sealedSubClasses = errorBody.sealedSubClasses,
                    ) { _, _ -> },
                )
            } ?: emptyMap(),
            headers = mapOf(),
        ),
    ),
)

private fun iFlowOperation(
    api: CreatedFlow,
    sender: CreatedFlow.Sender.Https,
    bodyInput: Body?,
    headersInput: List<Script.Header>,
    bodyOutput: Body?,
    headersOutput: Map<String, Header>,
    errorBody: Body?,
): Operation = Operation(
    summary = api.description,
    id = api.rawId,
    requestBody = if (bodyInput != null && bodyInput.contentNegotiations.isNotEmpty()) {
        RequestBody(
            description = "",
            required = true,
            content = bodyInput.contentNegotiations.associate {
                val contentType = it.contentType
                if (contentType == "application/octet-stream") {
                    contentType to MediaType(null)
                } else {
                    it.contentType() to MediaType(
                        bodyInput.klass.toSchema(
                            kdoc = null,
                            nullable = false,
                            refOnly = true,
                            sealedSubClasses = bodyInput.sealedSubClasses,
                        ) { _, _ -> },
                    )
                }
            },
        )
    } else {
        null
    },
    tags = listOf(api.packageName),
    security = listOf(mapOf(sender.role to emptyList())),
    parameters = buildList {
        if (sender.csrfProtection) {
            add(
                OpenApi.Parameter(
                    name = "X-CSRF-Token",
                    description = "The CSRF Token fetched by executing ${api.rawId}_CsrfToken first.",
                    position = OpenApi.Parameter.Position.Header,
                    schema = STRING(
                        nullable = false,
                    ),
                    required = true,
                ),
            )
        }
        for (it in headersInput) {
            add(
                OpenApi.Parameter(
                    name = it.name,
                    description = it.documentation,
                    position = OpenApi.Parameter.Position.Header,
                    schema = it.klass.toSchema(
                        kdoc = null,
                        nullable = false,
                        refOnly = true,
                        sealedSubClasses = emptySet(),
                    ) { _, _ -> },
                    required = !it.nullable,
                ),
            )
        }
    },
    responses = mapOf(
        (
            if (bodyOutput == null || bodyOutput.contentNegotiations.isEmpty()) {
                "204"
            } else {
                "200"
            }
            ) to Response(
            description = "",
            content = bodyOutput?.contentNegotiations?.associate {
                val contentType = it.contentType()
                if (contentType == "application/octet-stream") {
                    contentType to MediaType(null)
                } else {
                    it.contentType() to MediaType(
                        bodyOutput.klass.toSchema(
                            kdoc = null,
                            nullable = false,
                            refOnly = true,
                            sealedSubClasses = bodyOutput.sealedSubClasses,
                        ) { _, _ -> },
                    )
                }
            } ?: emptyMap(),
            headers = buildMap {
                put(SAP_MESSAGEPROCESSINGLOGID, SAPCI_MESSAGEHEADER)
                putAll(headersOutput)
            },
        ),
        "403" to Response(
            description = "Authorization failed",
            content = mapOf("text/html" to MediaType()),
            headers = mapOf(),
        ),
        "4XX" to Response(
            description = "Unexpected Error",
            content = errorBody?.contentNegotiations?.associate {
                it.contentType() to MediaType(
                    errorBody.klass.toSchema(
                        kdoc = null,
                        nullable = false,
                        refOnly = true,
                        sealedSubClasses = errorBody.sealedSubClasses,
                    ) { _, _ -> },
                )
            } ?: emptyMap(),
            headers = mapOf(SAP_MESSAGEPROCESSINGLOGID to SAPCI_MESSAGEHEADER),
        ),
    ),
)

private fun MutableMap<String, Schema>.addType(
    type: Type,
    sealedSubClasses: Set<SealedSubClass>,
) {
    when (type) {
        is Builtin -> Unit

        is CodeGenTree.Enum -> addType(type)

        is NormalClass -> addType(type, sealedSubClasses)

        is Type.ARRAY -> addType(type.item, sealedSubClasses)

        is LIST -> addType(type.item, sealedSubClasses)

        is SET -> addType(type.item, sealedSubClasses)

        is MAP -> {
            addType(type.key, sealedSubClasses)
            addType(type.value, sealedSubClasses)
        }

        is Type.Parameter -> Unit

        is Type.STAR -> Unit

        is Unknown -> Unit
    }
}

private fun MutableMap<String, Schema>.addType(type: CodeGenTree.Enum) {
    this[type.qualifiedName] = type.toSchema()
}

private fun MutableMap<String, Schema>.addType(type: NormalClass, sealedParentClassRef: String?) {
    this[type.addGenericsToName()] =
        type.toSchema(
            refOnly = false,
            kdoc = type.documentation,
            sealedParentClassRef = sealedParentClassRef,
            sealedSubClasses = emptySet(),
        ) { name, schema ->
            this[name] = schema
        }
}

private fun MutableMap<String, Schema>.addType(type: NormalClass, sealedSubClasses: Set<SealedSubClass>) {
    this[type.addGenericsToName()] =
        type.toSchema(
            refOnly = false,
            kdoc = type.documentation,
            sealedParentClassRef = null,
            sealedSubClasses = sealedSubClasses,
        ) { name, schema ->
            this[name] = schema
        }
}

private fun Class.getName(): String = when (this) {
    is NormalClass -> addGenericsToName()
    is CodeGenTree.Enum -> qualifiedName
}

private fun NormalClass.addGenericsToName(): String = buildString {
    append(qualifiedName)
    for (type in types) {
        append(type.toSimpleName())
    }
}

private fun ClassName.toRef() = buildString {
    append("#/components/schemas/")
    if (packageName.isNotBlank()) {
        append(packageName)
        append(".")
    }
    append(names.joinToString("."))
    for (runtimeType in runtimeTypes) {
        append(runtimeType.toSimpleName())
    }
}

private fun Type.toSimpleName(): String = when (this) {
    is Class -> qualifiedName
    is Type.ARRAY -> "Array" + item.toSimpleName()
    Builtin.BOOLEAN -> "Boolean"
    Builtin.BYTEARRAY -> "ByteArray"
    Builtin.BYTESTRING -> "String"
    Builtin.CHARARRAY -> "CharArray"
    Builtin.DOUBLE -> "Double"
    Builtin.DURATION -> "Duration"
    Builtin.FILE -> "File"
    Builtin.FLOAT -> "Float"
    Builtin.INT -> "Int"
    Builtin.LONG -> "Long"
    Builtin.STRING -> "String"
    Builtin.UNIT -> "Unit"
    Builtin.UUID -> "Uuid"
    DateType.DATE -> "Date"
    DateType.INSTANT -> "Instant"
    is LIST -> "List" + item.toSimpleName()
    is SET -> "Set" + item.toSimpleName()
    is MAP -> "Map" + key.toSimpleName() + value.toSimpleName()
    is Type.Parameter -> error("Not supported $name")
    is Type.STAR -> error("Not supported")
    Builtin.BYTE -> "Byte"
    Builtin.CHAR -> "Char"
    Builtin.SHORT -> "Short"
    is Unknown -> error("Not supported")
}

private fun Type.toSchema(
    minLength: Int? = null,
    maxLength: Int? = null,
    kdoc: String?,
    refOnly: Boolean,
    nullable: Boolean,
    sealedSubClasses: Set<SealedSubClass>,
    add: (String, Schema) -> Unit,
): Schema = when (this) {
    Builtin.BOOLEAN -> BOOLEAN(
        description = kdoc,
        nullable = nullable,
    )

    Builtin.BYTEARRAY -> STRING(
        description = kdoc,
        minLength = minLength,
        maxLength = maxLength,
        nullable = nullable,
        format = STRING.Format.Binary,
    )

    Builtin.BYTESTRING -> STRING(
        description = kdoc,
        minLength = minLength,
        maxLength = maxLength,
        nullable = nullable,
        format = STRING.Format.Byte,
    )

    Builtin.CHARARRAY -> error("Not possible $this")

    Builtin.DURATION -> STRING(
        description = kdoc,
        minLength = minLength,
        maxLength = maxLength,
        nullable = nullable,
        format = STRING.Format.Duration,
    )

    Builtin.FILE -> error("Not possible $this")

    Builtin.UNIT -> error("Not possible $this")

    Builtin.UUID -> STRING(
        description = kdoc,
        minLength = minLength,
        maxLength = maxLength,
        nullable = nullable,
        format = STRING.Format.Uuid,
    )

    Builtin.STRING -> STRING(
        description = kdoc,
        minLength = minLength,
        maxLength = maxLength,
        nullable = nullable,
        format = null,
    )

    Builtin.INT -> INT(
        format = INT.Format.Int32,
        description = kdoc,
        nullable = nullable,
    )

    Builtin.LONG -> INT(
        format = INT.Format.Int64,
        description = kdoc,
        nullable = nullable,
    )

    Builtin.DOUBLE -> NUMBER(
        format = NUMBER.Format.Double,
        description = kdoc,
        nullable = nullable,
    )

    Builtin.FLOAT -> NUMBER(
        format = NUMBER.Format.Float,
        description = kdoc,
        nullable = nullable,
    )

    is Type.ARRAY -> item.toArrayType(kdoc, refOnly, false, sealedSubClasses, add)

    is LIST -> item.toArrayType(kdoc, refOnly, false, sealedSubClasses, add)

    is SET -> item.toArrayType(kdoc, refOnly, true, sealedSubClasses, add)

    is MAP -> {
        require(key is Builtin.STRING) {
            "Only String are supported as Map keys"
        }
        OBJECT(
            description = kdoc,
            additionalProperties = json.encodeToJsonElement(
                Schema.serializer(),
                value.toSchema(
                    kdoc = null,
                    minLength = null,
                    maxLength = null,
                    refOnly = true,
                    nullable = false, // https://github.com/hfhbd/kfx/issues/219
                    sealedSubClasses = emptySet(),
                    add = add,
                ),
            ),
        )
    }

    DateType.DATE -> STRING(
        description = kdoc,
        format = STRING.Format.Date,
        minLength = 10,
        maxLength = 10,
        nullable = nullable,
    )

    DateType.INSTANT -> STRING(
        description = kdoc,
        format = STRING.Format.DateTime,
        minLength = 20,
        maxLength = 30,
        nullable = nullable,
    )

    is NormalClass -> toSchema(
        refOnly = refOnly,
        kdoc = kdoc,
        sealedParentClassRef = null,
        sealedSubClasses = sealedSubClasses,
        add = add,
    )

    is Unknown -> OBJECT(
        additionalProperties = JsonPrimitive(true),
    )

    is CodeGenTree.Enum -> {
        if (refOnly) {
            OBJECT(
                ref = "#/components/schemas/$qualifiedName",
                description = kdoc,
            )
        } else {
            toSchema()
        }
    }

    is Type.Parameter -> error("Not supported $this")

    is Type.STAR -> error("Not supported $this")

    Builtin.BYTE -> INT(
        format = INT.Format.Int8,
        description = kdoc,
        nullable = nullable,
    )

    Builtin.CHAR -> STRING(
        description = kdoc,
        nullable = nullable,
    )

    Builtin.SHORT -> INT(
        format = INT.Format.Int16,
        description = kdoc,
        nullable = nullable,
    )
}

private fun CodeGenTree.Enum.toSchema(): STRING = STRING(
    description = documentation,
    format = null,
    enum = values.map {
        it.serialName ?: it.name
    },
)

private fun NormalClass.toSchema(
    refOnly: Boolean,
    kdoc: String?,
    sealedParentClassRef: String?,
    sealedSubClasses: Set<SealedSubClass>,
    add: (String, Schema) -> Unit,
): OBJECT = if (refOnly) {
    OBJECT(
        description = kdoc,
        ref = "#/components/schemas/${addGenericsToName()}",
    )
} else if (sealedParentClassRef != null) {
    OBJECT(
        allOf = OneOf(
            listOf(
                OBJECT(ref = sealedParentClassRef),
                toNormalSchema(kdoc, sealedSubClasses, add),
            ),
        ),
    )
} else {
    toNormalSchema(kdoc, sealedSubClasses, add)
}

private fun NormalClass.toNormalSchema(
    kdoc: String?,
    sealedSubClasses: Set<SealedSubClass>,
    add: (String, Schema) -> Unit,
): OBJECT {
    val required = buildList {
        if (isSealed) {
            add("type")
        }
        for (member in members) {
            if (!member.nullable) {
                add(member.serialName ?: member.name)
            }
        }
    }

    return OBJECT(
        discriminator = if (isSealed) {
            OBJECT.Discriminator(
                propertyName = "type",
                mapping = buildMap {
                    for (sealedSubClass in sealedSubClasses) {
                        if (sealedSubClass.parent == asClassName()) {
                            val serialName = sealedSubClass.klass.serialName
                            if (serialName != null) {
                                put(serialName, sealedSubClass.klass.asClassName().toRef())
                            }
                        }
                    }
                },
            )
        } else {
            null
        },
        properties = OBJECT.Properties(
            buildMap {
                if (isSealed) {
                    this["type"] = STRING()
                }
                for (member in members) {
                    val serialName = member.serialName
                    this[serialName ?: member.name] = toProperty(member, sealedSubClasses, refOnly = true, add)
                    when (val propertyType = member.type) {
                        is Builtin -> continue

                        is Type.ARRAY -> propertyType.item.toArrayType(kdoc = null, false, false, sealedSubClasses, add)

                        is LIST -> propertyType.item.toArrayType(kdoc = null, false, false, sealedSubClasses, add)

                        is SET -> propertyType.item.toArrayType(kdoc = null, false, true, sealedSubClasses, add)

                        is MAP -> {
                            propertyType.key.toArrayType(kdoc = null, false, false, sealedSubClasses, add)
                            propertyType.value.toArrayType(kdoc = null, false, false, sealedSubClasses, add)
                        }

                        is Type.Parameter, is Type.STAR -> continue

                        is NormalClass -> add(
                            propertyType.getName(),
                            toProperty(member, sealedSubClasses, refOnly = false, add),
                        )

                        is Unknown -> continue

                        is CodeGenTree.Enum -> add(
                            propertyType.getName(),
                            toProperty(member, sealedSubClasses, refOnly = false, add),
                        )
                    }
                }
            },
        ),
        required = required,
        description = kdoc,
    )
}

private fun NormalClass.asClassName(): ClassName = ClassName(
    packageName = packageName,
    names = names,
    runtimeTypes = types,
)

private fun toProperty(
    property: CodeGenTree.Member,
    sealedSubClasses: Set<SealedSubClass>,
    refOnly: Boolean,
    add: (String, Schema) -> Unit,
): Schema {
    val propertySchema = property.type.toSchema(
        minLength = property.minLength,
        maxLength = property.maxLength,
        kdoc = property.documentation,
        refOnly = refOnly,
        add = add,
        sealedSubClasses = sealedSubClasses,
        nullable = property.nullable,
    )
    return propertySchema
}

private fun Type.toArrayType(
    kdoc: String?,
    refOnly: Boolean,
    uniqueItems: Boolean,
    sealedSubClasses: Set<SealedSubClass>,
    add: (String, Schema) -> Unit,
): ARRAY = when (this) {
    is Builtin -> ARRAY(
        items = toSchema(refOnly = true, kdoc = null, nullable = false, sealedSubClasses = sealedSubClasses, add = add),
        description = kdoc,
        uniqueItems = uniqueItems,
    )

    is Type.ARRAY -> error("Not possible $this")

    is LIST -> error("Not possible $this")

    is SET -> error("Not possible $this")

    is MAP -> error("Not possible $this")

    is Type.Parameter -> error("Not possible $this")

    is Type.STAR -> error("Not possible $this")

    is Unknown -> error("Not possible $this")

    is CodeGenTree.Enum -> error("Not possible $this")

    is NormalClass -> {
        if (!refOnly) {
            val t = toSchema(
                refOnly = false,
                add = add,
                kdoc = null,
                sealedSubClasses = sealedSubClasses,
                sealedParentClassRef = null,
            )
            add(getName(), t)
        }
        ARRAY(
            maxItems = null,
            items = OBJECT(
                ref = "#/components/schemas/${addGenericsToName()}",
            ),
            description = kdoc,
            uniqueItems = uniqueItems,
        )
    }
}

private const val SAP_MESSAGEPROCESSINGLOGID = "sap_messageprocessinglogid"
private val SAPCI_MESSAGEHEADER = Header(schema = STRING(nullable = false))

internal fun Body.ContentNegotiation.contentType(): String = if (parameters.isEmpty()) {
    contentType
} else {
    buildString {
        append(contentType)
        parameters.toList().joinTo(
            buffer = this,
            separator = ";",
            prefix = "; ",
        ) { (key, value) ->
            "$key=$value"
        }
    }
}
