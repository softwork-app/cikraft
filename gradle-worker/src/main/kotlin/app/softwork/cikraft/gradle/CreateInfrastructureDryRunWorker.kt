package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder
import integrationFlows
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

public abstract class CreateInfrastructureDryRunWorker : WorkAction<CreateInfrastructureDryRunWorker.Params> {
    public interface Params : WorkParameters {
        public val expectedRawIFlowsIds: SetProperty<String>
        public val entryPoints: ConfigurableFileCollection
        public val outputFolder: DirectoryProperty
        public val stageNames: SetProperty<String>
    }

    override fun execute() {
        val outputFolder = parameters.outputFolder.get().asFile

        val builder = IntegrationFlowBuilder(
            scripts = parameters.entryPoints.files.flatMap {
                if (it.exists()) {
                    Json.decodeFromString(ListSerializer(Script.serializer()), it.readText())
                } else {
                    emptyList()
                }
            },
            outputFolder = outputFolder,
            stageNames = parameters.stageNames.get(),
        )
        builder.integrationFlows()

        val actualRawIFlowIds = builder.createdFlows.map { it.rawId }.toSet()
        require(actualRawIFlowIds == parameters.expectedRawIFlowsIds.get()) {
            val missingIFlows = parameters.expectedRawIFlowsIds.get() - actualRawIFlowIds
            "Dry run misses following iFlows to be called in integrationFlows(): $missingIFlows"
        }
    }
}
