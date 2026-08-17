package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*
import com.squareup.kotlinpoet.*

@ExperimentalKotlinPoetApi
public fun generateTypedKotlinStepBuilder(
    entryPoints: List<Script>,
): List<FileSpec> = entryPoints.map {
    createEntryPointFile(it)
}

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

internal val CREATED_FLOW_CONFIG =
    ClassName("app.softwork.cikraft.integrationflow.builder", "CreatedFlowConfig")
internal val STEPBUILDER = ClassName("app.softwork.cikraft.integrationflow", "StepBuilder")
