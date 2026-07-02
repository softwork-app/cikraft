package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

@ExperimentalKotlinPoetApi
public fun generateTypedKotlinFlows(
    packageName: String,
    packageDescription: String?,
    flowName: String,
    flowDescription: String?,
    flowSource: List<String>,
    flowTarget: List<String>,
    suffixID: String?,
    baseUrl: String,
    entryPoints: List<Script>,
    groovyScripts: List<String>,
): List<FileSpec> {
    val flowFile = FileSpec.builder("", flowName)

    val flowID = flowName.replace("_", "")
    val flowIDUppercase = flowID.replaceFirstChar { it.uppercaseChar() }

    val packageID = packageName.replace("_", "")

    val flowObject = TypeSpec.objectBuilder(flowIDUppercase).apply {
        addModifiers(KModifier.DATA)
        addSuperinterface(CREATED_FLOW_CONFIG)

        for (entry in entryPoints) {
            addSuperinterface(ClassName("", entry.name))
        }
        for (groovyScript in groovyScripts) {
            addSuperinterface(ClassName("", groovyScript))
        }

        addProperty(
            PropertySpec.builder("baseUrl", STRING)
                .initializer("%S", baseUrl)
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
        addProperty(
            PropertySpec.builder("allowedHeaders", MUTABLE_SET.parameterizedBy(STRING))
                .initializer(
                    "%M(%S, %S)",
                    MemberName("kotlin.collections", "mutableSetOf"),
                    "Accept",
                    "Content-Type",
                )
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
        addProperty(
            PropertySpec.builder(
                "parameters",
                MUTABLE_MAP.parameterizedBy(
                    STRING,

                    LambdaTypeName.get(receiver = null, ClassName("", "Stage"), returnType = ANY),
                ),
            )
                .initializer("%M()", MemberName("kotlin.collections", "mutableMapOf"))
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
        addProperty(
            PropertySpec
                .builder(
                    name = "suffix",
                    type = STRING.copy(nullable = true),
                )
                .initializer(if (suffixID == null) "%L" else "%S", suffixID)
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
    }.build()
    flowFile.addType(flowObject)

    val flowObjectTypeName = ClassName("", flowIDUppercase)

    val flowFunction = FunSpec.builder(flowName)
    flowFunction.receiver(ClassName("app.softwork.cikraft.integrationflow.builder", "IntegrationFlowBuilder"))
    flowFunction.addParameter(
        "builder",
        LambdaTypeName.get(
            receiver = ClassName(
                "app.softwork.cikraft.integrationflow",
                "EndpointBuilder",
            ),
            contextParameters = listOf(flowObjectTypeName),
            returnType = UNIT,
        ),
    )

    flowFunction.beginControlFlow(
        """createPackageAndFlow(
            |packageId = %S,
            |packageName = %S,
            |packageDescription = %L,
            |integrationFlowId = %S,
            |integrationFlowIdRaw = %S,
            |integrationFlowName = %S,
            |integrationFlowNameRaw = %S,
            |integrationFlowDescription = %L,
            |integrationFlowSource = %M(%L),
            |integrationFlowTarget = %M(%L),
            |config = %N,
            |)
        """.trimMargin(),
        packageID,
        packageName,
        CodeBlock.of("%S", packageDescription),
        flowID + (suffixID ?: ""),
        flowID,
        flowName + if (suffixID != null) "_$suffixID" else "",
        flowName,
        CodeBlock.of("%S", flowDescription),
        MemberName("kotlin.collections", "listOf", isExtension = true),
        flowSource.map { CodeBlock.of("%S", it) }.joinToCode(),
        MemberName("kotlin.collections", "listOf", isExtension = true),
        flowTarget.map { CodeBlock.of("%S", it) }.joinToCode(),
        flowObject,
    )
    flowFunction.addStatement("builder(%T, this)", flowObjectTypeName)
    flowFunction.endControlFlow()

    flowFile.addFunction(flowFunction.build())

    val entryPointFiles = entryPoints.map {
        createEntryPointFile(it)
    }

    val groovyScriptsFiles = groovyScripts.map {
        createGroovyEntryPointFile(it)
    }

    return listOf(flowFile.build()) + entryPointFiles + groovyScriptsFiles
}

@ExperimentalKotlinPoetApi
private fun createGroovyEntryPointFile(groovyFileName: String) = FileSpec.builder("", groovyFileName).apply {
    addType(TypeSpec.interfaceBuilder(groovyFileName).addSuperinterface(CREATED_FLOW_CONFIG).build())

    val function = FunSpec.builder(groovyFileName)
    function.contextParameters(listOf(ContextParameter("config", ClassName("", groovyFileName))))

    function.receiver(STEPBUILDER)

    function.addStatement(
        "groovyScript(name = %S, file = %S)",
        groovyFileName,
        "$groovyFileName.groovy",
    )
    addFunction(function.build())
}.build()

@ExperimentalKotlinPoetApi
private fun createEntryPointFile(
    entryPoint: Script,
): FileSpec {
    val file = FileSpec.builder("", entryPoint.name)
    file.addType(TypeSpec.interfaceBuilder(entryPoint.name).addSuperinterface(CREATED_FLOW_CONFIG).build())

    file.addFunction(createEntryPointFunction(entryPoint = entryPoint))

    return file.build()
}

@ExperimentalKotlinPoetApi
private fun createEntryPointFunction(
    entryPoint: Script,
): FunSpec {
    val entryPointFunction = FunSpec.builder(entryPoint.name)
    entryPointFunction.contextParameters(listOf(ContextParameter("config", ClassName("", entryPoint.name))))

    entryPointFunction.receiver(STEPBUILDER)

    val properties: MutableList<Pair<Script.ParameterInput, TypeName>> = mutableListOf()
    val injected = mutableListOf<String>()

    for (it in entryPoint.inputs) {
        when (it) {
            is Script.Parameter -> {
                properties.add(it to it.klass.toKotlinPoet(it.nullable))
            }

            is Script.Property if (it.klass.isDataSource()) -> {
                properties.add(it to STRING)
            }

            is Script.Property -> Unit

            is Script.Body -> Unit

            is Script.Header -> Unit

            is Script.None -> when {
                it.hasDefault -> Unit
                it.klass.isMessage() -> Unit
                it.klass.isMessageLog() -> Unit
                it.klass.isKeyManager() -> Unit
                else -> injected.add(it.propertyName)
            }

            is Script.Password -> properties.add(it to STRING)
        }
    }

    for ((property, propertyType) in properties) {
        entryPointFunction.addParameter(
            ParameterSpec.builder(
                property.propertyName,
                LambdaTypeName.get(
                    receiver = null,
                    ClassName("", "Stage"),
                    returnType = propertyType.copy(nullable = false),
                ).copy(
                    nullable = propertyType.isNullable,
                ),
            ).apply {
                val documentation = property.documentation
                if (documentation != null) {
                    addKdoc(documentation)
                }
            }.build(),
        )
    }

    for (injected in injected) {
        entryPointFunction.addParameter(
            ParameterSpec.builder(
                name = injected,
                type = LambdaTypeName.get(
                    receiver = STEPBUILDER,
                    returnType = UNIT,
                ),
            ).build(),
        )
    }

    for ((property, propertyType) in properties) {
        if (propertyType.isNullable) {
            entryPointFunction.beginControlFlow("if (${property.propertyName} != null)")
        }
        entryPointFunction.addStatement("config.parameters[%S] = ${property.propertyName}", property.propertyName)
        if (propertyType.isNullable) {
            entryPointFunction.endControlFlow()
        }
    }

    for (injected in injected) {
        entryPointFunction.beginControlFlow("withPrefix(%S)", "Injected_")
        entryPointFunction.addStatement("%N()", injected)
        entryPointFunction.endControlFlow()
        entryPointFunction.beginControlFlow("contentModifier(%S)", "Set $injected for ${entryPoint.name}")
        entryPointFunction.addStatement("property(%S, %S)", injected, "_RESULT_")
        entryPointFunction.endControlFlow()
    }

    if (properties.isNotEmpty()) {
        entryPointFunction.beginControlFlow("contentModifier(%S)", "Properties for ${entryPoint.name}")
        for ((property, propertyType) in properties) {
            if (propertyType.isNullable) {
                entryPointFunction.beginControlFlow("if (${property.propertyName} != null)")
            }
            entryPointFunction.addStatement("externalParameter(%S)", property.propertyName)
            if (propertyType.isNullable) {
                entryPointFunction.endControlFlow()
            }
        }
        entryPointFunction.endControlFlow()
    }

    for (input in entryPoint.inputs) {
        when (input) {
            is Script.Header -> {
                entryPointFunction.addStatement("config.allowedHeaders.add(%S)", input.name)
            }

            is Script.Body,
            is Script.None,
            is Script.Password,
            is Script.Property,
            is Script.Parameter,
            -> continue
        }
    }

    entryPointFunction.addStatement(
        "groovyScript(name = %S, function = %S, file = %S)",
        entryPoint.name,
        entryPoint.name,
        "entrypoints.groovy",
    )

    return entryPointFunction.build()
}

private val CREATED_FLOW_CONFIG =
    ClassName("app.softwork.cikraft.integrationflow.builder", "CreatedFlowConfig")
private val STEPBUILDER = ClassName("app.softwork.cikraft.integrationflow", "StepBuilder")
