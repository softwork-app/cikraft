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
        public val entryPoints: ConfigurableFileCollection
        public val outputFolder: DirectoryProperty
        public val stageNames: SetProperty<String>
    }

    override fun execute() {
        val outputFolder = parameters.outputFolder.get().asFile

        IntegrationFlowBuilder(
            scripts = parameters.entryPoints.files.flatMap {
                Json.decodeFromString(ListSerializer(Script.serializer()), it.readText())
            },
            outputFolder = outputFolder,
            stageNames = parameters.stageNames.get(),
        ).integrationFlows()
    }
}
