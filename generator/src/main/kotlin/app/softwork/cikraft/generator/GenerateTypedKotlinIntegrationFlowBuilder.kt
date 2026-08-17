package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

@ExperimentalKotlinPoetApi
public fun generateTypedKotlinIntegrationFlowBuilder(
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
): FileSpec {
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

    return flowFile.build()
}
