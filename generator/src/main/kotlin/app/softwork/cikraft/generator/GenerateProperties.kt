package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*
import app.softwork.cikraft.core.Script.*
import com.squareup.kotlinpoet.*

public fun generateProperties(
    createdFlow: CreatedFlow,
    parameters: Map<String, Value>,
): FileSpec {
    val packageName = createdFlow.packageName.toPackageName()

    val file = FileSpec.builder(packageName, createdFlow.rawId)

    val configObject = TypeSpec.objectBuilder(createdFlow.rawId + "Config")
    configObject.addModifiers(KModifier.DATA)

    val parameterInput = (createdFlow.scripts + createdFlow.injectedScripts)
        .flatMap { it.inputs }
        .distinctBy { it.propertyName }
        .filterIsInstance<ParameterInput>()

    for (input in parameterInput) {
        configObject.addProperty(
            input,
            iFlowRawName = createdFlow.rawName,
            defaultValue = if (input is Password) {
                null
            } else {
                parameters[input.propertyName]
            },
        )
    }

    file.addType(configObject.build())
    return file.build()
}

private fun TypeSpec.Builder.addProperty(
    input: ParameterInput,
    iFlowRawName: String,
    defaultValue: Value?,
) {
    if (input.klass.isDataSource() || input.klass.isKeyManager()) {
        return
    }

    val nullable = when (input.nullable) {
        false -> false

        true -> when (defaultValue) {
            null -> true
            else -> false
        }
    }

    val type = input.klass.toKotlinPoet(nullable = nullable) as ClassName

    addProperty(
        ktorProperty(
            input,
            iFlowRawName,
            type,
            defaultValue,
        ),
    )
}

private fun ktorProperty(input: Input, iFlowRawName: String, type: ClassName, defaultValue: Value?): PropertySpec {
    val property = PropertySpec.builder(input.propertyName, type)
    val convertType = (type.copy(nullable = false) as ClassName)

    val defaultValueBlock: CodeBlock = when (defaultValue) {
        is Value.BOOLEAN -> CodeBlock.of(" ?: %L", defaultValue.value)
        is Value.DOUBLE -> CodeBlock.of(" ?: %L", defaultValue.value)
        is Value.FLOAT -> CodeBlock.of(" ?: %L", defaultValue.value)
        is Value.INT -> CodeBlock.of(" ?: %L", defaultValue.value)
        is Value.STRING -> CodeBlock.of(" ?: %S", defaultValue.value)
        null -> CodeBlock.of("")
    }

    property.getter(
        FunSpec.getterBuilder().addStatement(
            """return %M(%S)%L%L%L""",
            MemberName("app.softwork.cikraft.ktor.server.runtime", "env", isExtension = true),
            "${iFlowRawName}_${input.propertyName}".uppercase(),
            if (defaultValue != null) "" else "!!",
            convertType.convertFromString(nullable = defaultValue != null),
            defaultValueBlock,
        ).build(),
    )
    return property.build()
}
