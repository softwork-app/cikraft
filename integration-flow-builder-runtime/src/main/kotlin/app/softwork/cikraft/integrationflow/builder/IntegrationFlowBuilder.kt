package app.softwork.cikraft.integrationflow.builder

import app.softwork.cikraft.core.*
import app.softwork.cikraft.integrationflow.CreateArtifact.Companion.definitionsXML
import app.softwork.cikraft.integrationflow.Definitions
import app.softwork.cikraft.integrationflow.EndpointBuilder
import app.softwork.cikraft.integrationflow.integrationFlow
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

public class IntegrationFlowBuilder(
    private val scripts: List<Script>,
    private val outputFolder: File?,
    private val stageNames: Set<String>,
) {
    public val createdFlows: List<CreatedFlow>
        field = mutableListOf()

    public fun createPackageAndFlow(
        packageId: String,
        packageName: String,
        packageDescription: String?,
        integrationFlowId: String,
        integrationFlowIdRaw: String,
        integrationFlowName: String,
        integrationFlowNameRaw: String,
        integrationFlowDescription: String?,
        integrationFlowSource: List<String>,
        integrationFlowTarget: List<String>,
        config: CreatedFlowConfig,
        builder: EndpointBuilder.() -> Unit,
    ) {
        val integrationFlow = config.integrationFlow {
            if (integrationFlowSource.isNotEmpty()) {
                sender = integrationFlowSource.joinToString()
            }
            if (integrationFlowTarget.isNotEmpty()) {
                receiver = integrationFlowTarget.joinToString()
            }
            builder()
        }

        val normalCallActivities = integrationFlow.process.callActivities.filter {
            it.id.startsWith("GroovyScript_")
        }
        val injectedCallActivities = integrationFlow.process.callActivities.filter {
            it.id.startsWith("Injected_GroovyScript_")
        }
        val scriptJvmFunctions = normalCallActivities.map {
            it.extensionElements.properties.single { it.key == "scriptFunction" }.value
        }
        val injected = injectedCallActivities.map {
            it.extensionElements.properties.single { it.key == "scriptFunction" }.value
        }

        val createdFlow = CreatedFlow(
            id = integrationFlowId,
            rawId = integrationFlowIdRaw,
            name = integrationFlowName,
            rawName = integrationFlowNameRaw,
            description = integrationFlowDescription,
            source = integrationFlowSource,
            target = integrationFlowTarget,
            packageID = packageId,
            packageName = packageName,
            packageDescription = packageDescription,
            sender = integrationFlow.collaboration.messageFlow?.toSender(config),
            scripts = scriptJvmFunctions.mapNotNull { userScript ->
                scripts.singleOrNull { it.name == userScript }
            },
            injectedScripts = injected.mapNotNull { userInjectedScript ->
                scripts.singleOrNull { it.name == userInjectedScript }
            },
        )

        if (outputFolder != null) {
            File(
                File(outputFolder, "flows").apply {
                    mkdirs()
                },
                "${createdFlow.rawName}.json",
            )
                .writeText(
                    Json.encodeToString(CreatedFlow.serializer(), createdFlow) + "\n",
                )
            File(
                File(outputFolder, "definitions").apply {
                    mkdirs()
                },
                "${createdFlow.rawName}.xml",
            ).writeText(
                definitionsXML.encodeToString(Definitions.serializer(), integrationFlow) + "\n",
            )
            for (stageName in stageNames) {
                val stage = Stage.valueOf(stageName)
                val parameters = config.parameters.mapValues { (_, stageFunction) ->
                    when (val value = stageFunction(stage)) {
                        is String -> Value.STRING(value)
                        is Int -> Value.INT(value)
                        is Double -> Value.DOUBLE(value)
                        is Float -> Value.FLOAT(value)
                        is Boolean -> Value.BOOLEAN(value)
                        else -> error("Not supported '$value' of ${value.javaClass}")
                    }
                }

                File(
                    File(outputFolder, "properties/$stageName").apply {
                        mkdirs()
                    },
                    "${createdFlow.rawName}.properties",
                ).writeText(
                    Json.encodeToString(
                        MapSerializer(String.serializer(), Value.serializer()),
                        parameters,
                    ) + "\n",
                )
            }
        }
        createdFlows.add(createdFlow)
    }
}
