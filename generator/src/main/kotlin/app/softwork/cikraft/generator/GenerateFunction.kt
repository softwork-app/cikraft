package app.softwork.cikraft.generator

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.core.Script
import app.softwork.cikraft.core.Script.Body
import app.softwork.cikraft.core.Script.DynamicHeaders
import app.softwork.cikraft.core.Script.Header
import app.softwork.cikraft.core.Script.Input
import app.softwork.cikraft.core.Script.None
import app.softwork.cikraft.core.Script.Password
import app.softwork.cikraft.core.Script.Property
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode

public fun generateFunction(
    createdFlow: CreatedFlow,
): FileSpec {
    val packageName = createdFlow.packageName.toPackageName()
    val file = FileSpec.builder(packageName, createdFlow.rawId + "Function")
    val function = FunSpec.builder(createdFlow.rawId + "Function")

    if (createdFlow.scripts.any { it.isSuspend }) {
        function.addModifiers(KModifier.SUSPEND)
    }
    val outputs = getBodyAndHeader((createdFlow.scripts))
    if (outputs.isNotEmpty()) {
        val isNullable = createdFlow.scripts.any {
            it.outputIsNullable
        }
        function.returns(
            ClassName(packageName, createdFlow.resultClassName).copy(
                nullable = isNullable,
            ),
        )
    }

    val parameters = function.callScripts(createdFlow.scripts, hasOutputs = outputs.isNotEmpty())
    function.addParameters(parameters.distinctBy { it.propertyName })

    if (outputs.isNotEmpty()) {
        function.returnResultClass(createdFlow)
    }

    file.addFunction(function.build())

    if (outputs.isNotEmpty()) {
        val resultClass = TypeSpec.classBuilder(createdFlow.resultClassName)

        resultClass.addModifiers(KModifier.DATA)
        resultClass.addBodyAndHeader(outputs)

        file.addType(resultClass.build())
    }

    return file.build()
}

private val CreatedFlow.resultClassName: String get() = rawId.replaceFirstChar { it.uppercaseChar() } + "Result"

private fun FunSpec.Builder.addParameters(
    inputs: List<Input>,
) {
    for (input in inputs) {
        addParameter(
            ParameterSpec.builder(
                name = input.propertyName,
                type = input.klass.toKotlinPoet(input.nullable),
            ).apply {
                if (input.nullable) {
                    defaultValue("null")
                }
            }.build(),
        )
    }
}

private val Script.resultName get() = "result${name.replaceFirstChar { it.uppercase() }}"

private fun TypeSpec.Builder.addBodyAndHeader(bodyAndHeaders: BodyAndHeaders) {
    val constructor = FunSpec.constructorBuilder()
    if (bodyAndHeaders.body != null) {
        constructor.addParameter(
            name = bodyAndHeaders.body.propertyName,
            type = bodyAndHeaders.body.klass.toKotlinPoet(bodyAndHeaders.body.nullable),
        )
        addProperty(
            PropertySpec.builder(
                name = bodyAndHeaders.body.propertyName,
                type = bodyAndHeaders.body.klass.toKotlinPoet(bodyAndHeaders.body.nullable),
            ).initializer(bodyAndHeaders.body.propertyName)
                .build(),
        )
    }
    for (header in bodyAndHeaders.headers) {
        constructor.addParameter(
            name = header.propertyName,
            type = header.klass.toKotlinPoet(header.nullable),
        )
        addProperty(
            PropertySpec.builder(
                name = header.propertyName,
                type = header.klass.toKotlinPoet(header.nullable),
            ).initializer(header.propertyName)
                .build(),
        )
    }
    for (header in bodyAndHeaders.dynamicHeaders) {
        constructor.addParameter(
            name = header.propertyName,
            type = header.klass.toKotlinPoet(header.nullable),
        )
        addProperty(
            PropertySpec.builder(
                name = header.propertyName,
                type = header.klass.toKotlinPoet(header.nullable),
            ).initializer(header.propertyName)
                .build(),
        )
    }
    primaryConstructor(constructor.build())
}

internal data class BodyAndHeaders(
    val body: Body?,
    val headers: Collection<Header>,
    val dynamicHeaders: Collection<DynamicHeaders>,
) {
    fun isNotEmpty() = body != null || headers.isNotEmpty() || dynamicHeaders.isNotEmpty()
}

private fun getBodyAndHeader(scripts: List<Script>): BodyAndHeaders {
    var body: Body? = null
    val headers = mutableListOf<Header>()
    val dynamicHeaders = mutableListOf<DynamicHeaders>()

    for (script in scripts) {
        for (output in script.outputs) {
            when (output) {
                is Body if output.contentNegotiations.isNotEmpty() -> body = output
                is Body -> continue
                is DynamicHeaders -> dynamicHeaders.add(output)
                is Header -> headers += output
                is Property -> continue
            }
        }
    }
    return BodyAndHeaders(
        body = body,
        headers = headers.reversed().distinctBy { it.name },
        dynamicHeaders = dynamicHeaders.reversed().distinctBy { it.propertyName },
    )
}

private fun FunSpec.Builder.returnResultClass(createdFlow: CreatedFlow) {
    var bodyIsSet = false
    val headerNames = mutableSetOf<String>()

    val returnParameters = mutableListOf<CodeBlock>()
    for (script in createdFlow.scripts.reversed()) {
        for (output in script.outputs) {
            when (output) {
                is Body if bodyIsSet -> continue

                is Body if output.contentNegotiations.isNotEmpty() -> {
                    returnParameters.add(
                        CodeBlock.of("${output.propertyName} = ${script.resultName}.${output.propertyName}"),
                    )
                    bodyIsSet = true
                }

                is Body -> {}

                is DynamicHeaders -> {
                    returnParameters.add(
                        CodeBlock.of("${output.propertyName} = ${script.resultName}.${output.propertyName}"),
                    )
                }

                is Header if output.name !in headerNames -> {
                    returnParameters.add(
                        CodeBlock.of("${output.propertyName} = ${script.resultName}.${output.propertyName}"),
                    )
                    headerNames.add(output.name)
                }

                is Header -> continue

                is Property -> continue
            }
        }
    }
    addStatement("return %L(%L)", createdFlow.resultClassName, returnParameters.joinToCode())
}

private fun FunSpec.Builder.callScripts(scripts: List<Script>, hasOutputs: Boolean): List<Input> {
    val parameters = mutableListOf<Input>()

    for ((index, entryPoint) in scripts.withIndex()) {
        addStatement(
            "%L%M(%L)%L",
            if (entryPoint.outputJvmName == "kotlin.Unit") {
                CodeBlock.of("")
            } else {
                CodeBlock.of("val ${entryPoint.resultName} = ")
            },
            MemberName(entryPoint.packageName, entryPoint.name, isExtension = true),
            CodeBlock.builder().apply {
                for (input in entryPoint.inputs) {
                    when (input) {
                        is Body -> {
                            val previousScriptBody = scripts.subList(0, index).lastOrNull {
                                it.bodyOutput != null
                            }
                            if (previousScriptBody != null) {
                                val body = previousScriptBody.bodyOutput!!
                                add(
                                    "${input.propertyName} = ${previousScriptBody.resultName}.${body.propertyName},",
                                )
                            } else {
                                add("${input.propertyName} = ${input.propertyName},")
                                parameters.add(input)
                            }
                        }

                        is Header -> {
                            val previousEntrypoint = scripts.subList(0, index).filter {
                                it.outputJvmName != "kotlin.Unit"
                            }.singleOrNull {
                                it.outputs.any { it is Header && it.name == input.name }
                            }

                            val previousOutputHeader =
                                previousEntrypoint?.outputs?.single { it is Header && it.name == input.name }
                            if (previousOutputHeader != null) {
                                add(
                                    "${input.propertyName} = ${previousEntrypoint.resultName}.${previousOutputHeader.propertyName},",
                                )
                            } else {
                                add("${input.propertyName} = ${input.propertyName},")
                                parameters.add(input)
                            }
                        }

                        is Property,
                        is Script.Parameter,
                        -> {
                            val previousEntrypoint = scripts.subList(0, index).filter {
                                it.outputJvmName != "kotlin.Unit"
                            }.singleOrNull {
                                it.outputs.any { it is Property && it.propertyName == input.propertyName }
                            }

                            val previousOutputProperty =
                                previousEntrypoint?.outputs?.single {
                                    it is Property &&
                                        it.propertyName == input.propertyName
                                }
                            if (previousOutputProperty != null) {
                                add(
                                    "${input.propertyName} = ${previousEntrypoint.resultName}.${previousOutputProperty.propertyName},",
                                )
                            } else {
                                add("${input.propertyName} = ${input.propertyName},")
                                parameters.add(input)
                            }
                        }

                        is None,
                        is Password,
                        -> {
                            add("${input.propertyName} = ${input.propertyName},")
                            parameters.add(input)
                        }
                    }
                }
            }.build(),
            if (entryPoint.outputIsNullable) {
                CodeBlock.of(" ?: return" + if (hasOutputs) " null" else "")
            } else {
                CodeBlock.of("")
            },
        )
    }
    return parameters
}
