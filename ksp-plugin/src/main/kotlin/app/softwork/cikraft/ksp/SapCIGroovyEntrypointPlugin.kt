package app.softwork.cikraft.ksp

import app.softwork.cikraft.core.*
import app.softwork.cikraft.core.Script.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.*
import io.github.hfhbd.kfx.codegen.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.*
import kotlinx.serialization.json.*

@KspExperimental
public class SapCIGroovyEntrypointPlugin(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val scripts = mutableListOf<Script>()

        val bodyAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.Body")
        val contentTypeAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.ContentType")
        val propertyAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.Property")
        val parameterAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.Parameter")
        val headerAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.Header")

        val dynamicHeadersAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.DynamicHeaders")

        val passwordAnnotation = resolver.getKSNameFromString("app.softwork.cikraft.Password")

        val throwsAnnotation = resolver.getKSNameFromString("kotlin.Throws")

        val scriptEntries = resolver.getSymbolsWithAnnotation("app.softwork.cikraft.ScriptEntry").toList()

        for (scriptEntry in scriptEntries) {
            require(scriptEntry is KSFunctionDeclaration)

            val params = buildList {
                for (param in scriptEntry.parameters) {
                    val annos = param.annotations.toList()
                    if (annos.isNotEmpty()) {
                        for (anno in annos) {
                            add(
                                when (anno.shortName.getShortName()) {
                                    bodyAnnotation.getShortName() -> {
                                        getBody(
                                            paramType = param.type,
                                            paramName = param.name!!.asString(),
                                            anno = anno,
                                            documentation = scriptEntry.docString(param),
                                            contentTypeAnnotation = contentTypeAnnotation,
                                        )
                                    }

                                    propertyAnnotation.getShortName() -> {
                                        val (klass, nullable) = param.type.resolveJvmType(
                                            properties = false,
                                        ) { error("Not supported") }
                                        val propertyName = param.name!!.getShortName()
                                        Script.Property(
                                            name = anno.stringValue.ifBlank { propertyName },
                                            propertyName = propertyName,
                                            klass = klass,
                                            nullable = nullable,
                                            documentation = scriptEntry.docString(param),
                                        )
                                    }

                                    parameterAnnotation.getShortName() -> {
                                        val (klass, nullable) = param.type.resolveJvmType(
                                            properties = false,
                                        ) { error("Not supported") }
                                        require(klass is Type.Builtin) {
                                            "Only builtin types are supported for @Parameter"
                                        }
                                        val propertyName = param.name!!.getShortName()
                                        Script.Parameter(
                                            propertyName = propertyName,
                                            klass = klass,
                                            nullable = nullable,
                                            documentation = scriptEntry.docString(param),
                                        )
                                    }

                                    headerAnnotation.getShortName() -> {
                                        val (klass, nullable) = param.type.resolveJvmType(
                                            properties = false,
                                        ) { error("Not supported") }
                                        Script.Header(
                                            name = anno.stringValue.ifBlank { param.name!!.getShortName() },
                                            propertyName = param.name!!.getShortName(),
                                            klass = klass as CodeGenTree.Type.Builtin,
                                            documentation = scriptEntry.docString(param),
                                            nullable = nullable,
                                        )
                                    }

                                    passwordAnnotation.getShortName() -> {
                                        Script.Password(
                                            propertyName = param.name!!.getShortName(),
                                            nullable = param.type.resolve().isMarkedNullable,
                                            documentation = scriptEntry.docString(param),
                                        )
                                    }

                                    else -> {
                                        val (klass, nullable) = param.type.resolveJvmType(properties = false) { }
                                        Script.None(
                                            propertyName = param.name!!.getShortName(),
                                            klass = klass,
                                            nullable = nullable,
                                            documentation = scriptEntry.docString(param),
                                            hasDefault = param.hasDefault,
                                        )
                                    }
                                },
                            )
                        }
                    } else {
                        val (klass, nullable) = param.type.resolveJvmType(properties = false) {}
                        add(
                            Script.None(
                                propertyName = param.name!!.getShortName(),
                                klass = klass,
                                nullable = nullable,
                                documentation = scriptEntry.docString(param),
                                hasDefault = param.hasDefault,
                            ),
                        )
                    }
                }
            }

            val outputType = scriptEntry.returnType!!.resolve()
            val outputs = outputType.getOutputsOfType(
                bodyAnnotation,
                dynamicHeadersAnnotation,
                headerAnnotation,
                propertyAnnotation,
                contentTypeAnnotation,
            )

            val throws = scriptEntry.annotations.singleOrNull {
                it.shortName.asString() == throwsAnnotation.getShortName()
            }
            val error = if (throws != null) {
                @Suppress("UNCHECKED_CAST")
                val error = (throws.arguments.single().value as List<KSType>).single()
                Error(
                    packageName = error.declaration.packageName.asString(),
                    name = error.declaration.simpleName.asString(),
                    outputs = error.getOutputsOfType(
                        bodyAnnotation,
                        dynamicHeadersAnnotation,
                        headerAnnotation,
                        propertyAnnotation,
                        contentTypeAnnotation,
                    ),
                    documentation = scriptEntry.docStringThrows(),
                )
            } else {
                null
            }

            val script = Script(
                name = scriptEntry.simpleName.getShortName(),
                isSuspend = Modifier.SUSPEND in scriptEntry.modifiers,
                jvmFunction = requireNotNull(resolver.getOwnerJvmClassName(scriptEntry)) + "." + requireNotNull(
                    resolver.getJvmName(scriptEntry),
                ),
                inputs = params,
                outputs = outputs,
                outputJvmName = outputType.declaration.qualifiedName!!.asString(),
                error = error,
                outputIsNullable = outputType.isMarkedNullable,
            )

            scripts.add(script)
        }

        if (scripts.isNotEmpty()) {
            codeGenerator.createNewFileByPath(
                dependencies = Dependencies(
                    aggregating = true,
                    sources = scriptEntries.map { it.containingFile!! }.distinct().toTypedArray(),
                ),
                path = "cikraft/entrypoint",
                extensionName = "json",
            ).use {
                it.writer().use { it.write(Json.encodeToString(scripts)) }
            }
        }

        return emptyList()
    }

    private fun getBody(
        paramName: String,
        paramType: KSTypeReference,
        anno: KSAnnotation,
        documentation: String?,
        contentTypeAnnotation: KSName,
    ): Script.Body {
        val sealedSubclasses = mutableSetOf<SealedSubClass>()

        val factoryKlasses = buildList {
            add(anno.arguments.first().value as KSType)
            if (anno.arguments.size == 2) {
                addAll(anno.arguments[1].value as List<KSType>)
            }
        }
        val contentNegotiations = factoryKlasses.mapNotNull {
            val factory = (it.declaration as KSClassDeclaration).takeUnless { declaration ->
                declaration.qualifiedName!!.asString() == "kotlin.Nothing"
            } ?: return@mapNotNull null
            val contentType = factory.annotations.single {
                it.shortName.getShortName() == contentTypeAnnotation.getShortName()
            }

            @Suppress("UNCHECKED_CAST")
            val parameters = (contentType.arguments.last().value as List<String>).associate {
                val (key, value) = it.split("=")
                key to value
            }
            Body.ContentNegotiation(
                factoryKlass = factory.toType(emptyList(), false) {
                    error("Not supported")
                } as NormalClass,
                contentType = contentType.arguments.first().value as String,
                parameters = parameters,
            )
        }
        val resolvedType = if (contentNegotiations.isEmpty()) {
            paramType.resolveJvmType(properties = false) {
                error("Not supported")
            }
        } else {
            paramType.resolveJvmType(properties = true) {
                sealedSubclasses.add(it)
            }
        }

        return Script.Body(
            propertyName = paramName,
            klass = resolvedType.klass,
            nullable = resolvedType.nullable,
            contentNegotiations = contentNegotiations,
            documentation = documentation,
            sealedSubClasses = sealedSubclasses,
        )
    }

    private fun KSType.getOutputsOfType(
        bodyAnnotation: KSName,
        dynamicHeadersAnnotation: KSName,
        headerAnnotation: KSName,
        propertyAnnotation: KSName,
        contentTypeAnnotation: KSName,
    ): Set<Output> {
        val outputs = buildSet {
            val returnType = declaration
            require(returnType is KSClassDeclaration)

            for (param in returnType.getAllProperties()) {
                addProperty(
                    param,
                    bodyAnnotation,
                    dynamicHeadersAnnotation,
                    headerAnnotation,
                    propertyAnnotation,
                    contentTypeAnnotation,
                    returnType.docString,
                )
            }
        }
        return outputs
    }

    private fun MutableSet<Output>.addProperty(
        param: KSPropertyDeclaration,
        bodyAnnotation: KSName,
        dynamicHeadersAnnotation: KSName,
        headerAnnotation: KSName,
        propertyAnnotation: KSName,
        contentTypeAnnotation: KSName,
        docString: String?,
    ) {
        val name = param.simpleName.getShortName()
        for (anno in param.annotations) {
            add(
                when (anno.shortName.getShortName()) {
                    bodyAnnotation.getShortName() -> {
                        getBody(
                            paramType = param.type,
                            paramName = param.simpleName.asString(),
                            anno = anno,
                            documentation = docString?.doc(name),
                            contentTypeAnnotation = contentTypeAnnotation,
                        )
                    }

                    dynamicHeadersAnnotation.getShortName() -> Script.DynamicHeaders(
                        propertyName = name,
                        documentation = docString?.doc(name),
                    )

                    headerAnnotation.getShortName() -> {
                        val resolvedType = param.type.resolveJvmType(properties = false) { error("Not supported") }
                        Script.Header(
                            name = anno.stringValue.ifBlank { name },
                            propertyName = name,
                            documentation = docString?.doc(name),
                            klass = resolvedType.klass as Type.Builtin,
                            nullable = resolvedType.nullable,
                        )
                    }

                    propertyAnnotation.getShortName() -> {
                        val resolvedType = param.type.resolveJvmType(properties = false) { error("Not supported") }
                        Script.Property(
                            name = anno.stringValue.ifBlank { name },
                            propertyName = name,
                            documentation = docString?.doc(name),
                            klass = resolvedType.klass,
                            nullable = resolvedType.nullable,
                        )
                    }

                    else -> continue
                },
            )
        }
    }
}

private val KSAnnotation.stringValue get() = arguments.singleOrNull()?.value as String? ?: ""

private data class ResolvedType(val klass: Type, val nullable: Boolean)

private fun KSTypeReference.resolveJvmType(
    properties: Boolean,
    addSealedSubClass: (SealedSubClass) -> Unit,
): ResolvedType {
    val type: KSType = resolve()

    return type.toType(
        includeProperties = properties,
        addSealedSubClass,
    )
}

private fun KSType.toType(
    includeProperties: Boolean,
    addSealedSubClass: (SealedSubClass) -> Unit,
): ResolvedType = ResolvedType(
    klass = declaration.toType(
        arguments.map { if (it.variance == Variance.STAR) null else it.type!!.resolve() },
        includeProperties,
        addSealedSubClass,
    ),
    nullable = isMarkedNullable,
)

private fun KSDeclaration.toType(
    arguments: List<KSType?>,
    includeDetails: Boolean,
    addSealedSubClass: (SealedSubClass) -> Unit,
): Type {
    val name = qualifiedName!!

    return when (name.asString()) {
        "kotlin.String" -> Type.Builtin.STRING

        "kotlin.CharArray" -> Type.Builtin.CHARARRAY

        "kotlin.Boolean" -> Type.Builtin.BOOLEAN

        "kotlin.Int" -> Type.Builtin.INT

        "kotlin.Long" -> Type.Builtin.LONG

        "kotlin.Double" -> Type.Builtin.DOUBLE

        "kotlin.Float" -> Type.Builtin.FLOAT

        "kotlin.uuid.Uuid" -> Type.Builtin.UUID

        "kotlin.time.Duration" -> Type.Builtin.DURATION

        "kotlin.Unit" -> Type.Builtin.UNIT

        "kotlinx.datetime.LocalDate" -> Type.DateType.DATE

        "kotlin.time.Instant" -> Type.DateType.INSTANT

        "kotlin.collections.List" -> Type.LIST(arguments.single()!!.toType(true, addSealedSubClass).klass)

        "kotlin.Array" -> Type.ARRAY(arguments.single()!!.toType(true, addSealedSubClass).klass)

        "kotlin.collections.Set" -> Type.SET(arguments.single()!!.toType(true, addSealedSubClass).klass)

        "kotlin.collections.Map" -> Type.MAP(
            arguments.first()!!.toType(true, addSealedSubClass).klass,
            arguments.last()!!.toType(true, addSealedSubClass).klass,
        )

        "kotlinx.io.Source" -> NormalClass("kotlinx.io", listOf("Source"))

        "kotlinx.io.RawSource" -> NormalClass("kotlinx.io", listOf("RawSource"))

        "kotlinx.io.Sink" -> NormalClass("kotlinx.io", listOf("Sink"))

        "kotlinx.io.RawSink" -> NormalClass("kotlinx.io", listOf("RawSink"))

        "java.io.OutputStream" -> NormalClass("java.io", listOf("OutputStream"))

        "java.io.InputStream" -> NormalClass("java.io", listOf("InputStream"))

        else -> {
            val isClassDeclaration = this is KSClassDeclaration

            val packageName = packageName.asString()

            val names = getOuterNames()

            if (isClassDeclaration) {
                val parentClassName = ClassName(packageName, names)
                for (sealedSubclass in getSealedSubclasses()) {
                    val sealedType = sealedSubclass.toType(
                        emptyList(),
                        includeDetails,
                        addSealedSubClass,
                    ) as CodeGenTree.NormalClass
                    addSealedSubClass(SealedSubClass(parentClassName, sealedType))
                }
            }

            val annotations = if (includeDetails) annotations.asAnnoMap() else emptyList()
            val documentation = docString?.takeWhile { it != '@' }?.trim()?.takeUnless { it.isBlank() }

            val isSealed = (this as? KSClassDeclaration)?.modifiers?.contains(Modifier.SEALED) == true
            val isEnum = (this as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
            val isObject = (this as? KSClassDeclaration)?.classKind == ClassKind.OBJECT

            if (isEnum) {
                CodeGenTree.Enum(
                    packageName = packageName,
                    names = names,
                    values = this.declarations.filter {
                        it is KSClassDeclaration && it.classKind == ClassKind.ENUM_ENTRY
                    }.map {
                        CodeGenTree.Enum.Value(
                            name = it.simpleName.asString(),
                            documentation = docString?.doc(it.simpleName.getShortName()),
                            annotations = it.annotations.asAnnoMap(),
                        )
                    }.toList(),
                    documentation = documentation,
                    annotations = annotations,
                )
            } else {
                NormalClass(
                    packageName = packageName,
                    names = names,
                    members = if (includeDetails && this is KSClassDeclaration) {
                        getDeclaredProperties()
                            .filter {
                                isSealed || it.hasBackingField
                            }.map {
                                it.toProperty(
                                    typeParameters,
                                    arguments,
                                    it.docString ?: docString?.doc(it.simpleName.getShortName()),
                                    addSealedSubClass,
                                )
                            }.toList()
                    } else {
                        emptyList()
                    },
                    documentation = documentation,
                    annotations = annotations,
                    functions = emptyList(),
                    types = List(typeParameters.size) { index ->
                        val argument = arguments[index]
                        argument?.toType(includeProperties = false) { }?.klass ?: Type.STAR
                    },
                    isFault = false,
                    isSealed = isSealed,
                    isStatic = isObject,
                )
            }
        }
    }
}

private fun KSDeclaration.getOuterNames(): List<String> = if (parentDeclaration == null) {
    listOf(simpleName.asString())
} else {
    parentDeclaration!!.getOuterNames() + listOf(simpleName.asString())
}

internal fun String.doc(property: String): String? = extractText("@param $property ")

private fun String.extractText(p: String): String? {
    val foundPropertyDoc = indexOf(p).takeUnless { it == -1 } ?: return null

    val next = indexOf("@", startIndex = foundPropertyDoc + 1 + p.length).takeUnless { it == -1 }
    return substring(foundPropertyDoc + p.length, next ?: length).trim()
}

private fun KSFunctionDeclaration.docString(param: KSValueParameter): String? =
    docString?.doc(param.name!!.getShortName())

private fun KSFunctionDeclaration.docStringThrows(): String? = docString?.extractText("@throws ")?.dropWhile {
    it != ' '
}?.drop(1)

private fun KSPropertyDeclaration.toProperty(
    classTypes: List<KSTypeParameter>,
    resolvedClassTypes: List<KSType?>,
    docString: String?,
    addSealedSubClass: (SealedSubClass) -> Unit,
): CodeGenTree.Member {
    val resolvedType = type.resolve()

    for ((index, classType) in classTypes.withIndex()) {
        if (classType.name == resolvedType.declaration.simpleName) {
            val realClass = resolvedClassTypes[index]!!

            val klass = realClass.declaration.toType(
                listOf(),
                true,
                addSealedSubClass,
            )

            return CodeGenTree.Member(
                name = simpleName.asString(),
                type = klass,
                annotations = annotations.asAnnoMap(),
                documentation = docString,
                nullable = resolvedType.isMarkedNullable,
            )
        }
    }

    val resolvedArguments: List<KSType?> = resolvedType.innerArguments.map {
        if (it.variance == Variance.STAR) {
            return@map null
        } else {
            val resolvedType = it.type!!.resolve()

            for ((index, classType) in classTypes.withIndex()) {
                if (classType.name == resolvedType.declaration.simpleName) {
                    return@map resolvedClassTypes[index]!!
                }
            }
            return@map resolvedType
        }
    }

    val klass = resolvedType.declaration.toType(
        resolvedArguments,
        true,
        addSealedSubClass,
    )

    return CodeGenTree.Member(
        name = simpleName.asString(),
        type = klass,
        annotations = annotations.asAnnoMap(),
        documentation = docString,
        nullable = resolvedType.isMarkedNullable,
    )
}

private fun Sequence<KSAnnotation>.asAnnoMap(): List<CodeGenTree.Annotation> = map {
    CodeGenTree.Annotation(
        packageName = it.annotationType.resolve().declaration.packageName.asString(),
        names = listOf(it.shortName.getShortName()),
        values = buildMap {
            for (it in it.arguments) {
                val value = when (val value = it.value) {
                    is Boolean -> Expression.BooleanLiteral(value)

                    is Byte -> error("Byte is not yet supported")

                    is Char -> error("Char is not yet supported")

                    is Short -> error("Short is not yet supported")

                    is Int -> CodeGenTree.Expression.IntLiteral(value)

                    is Long -> CodeGenTree.Expression.LongLiteral(value)

                    is Float -> CodeGenTree.Expression.FloatLiteral(value)

                    is Double -> CodeGenTree.Expression.DoubleLiteral(value)

                    is String -> CodeGenTree.Expression.StringLiteral(value)

                    is KSType -> CodeGenTree.Expression.ClassLiteral(
                        ClassName(
                            value.declaration.packageName.asString(),
                            value.declaration.simpleName.asString().split("."),
                        ),
                    )

                    is KSClassDeclaration -> error("Enum is not supported $value")

                    is KSAnnotation -> null

                    is Array<*> -> null

                    null -> null

                    else -> null
                }

                if (value != null) {
                    put(it.name?.getShortName() ?: "value", value)
                }
            }
        },
    )
}.toList()
