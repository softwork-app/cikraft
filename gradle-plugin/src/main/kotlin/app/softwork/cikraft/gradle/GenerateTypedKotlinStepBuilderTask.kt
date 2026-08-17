package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.generator.generateTypedKotlinStepBuilder
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.NormalizeLineEndings
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
public abstract class GenerateTypedKotlinStepBuilderTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val jsonScriptEntry: ConfigurableFileCollection

    @get:OutputDirectory
    public abstract val typedKotlinFlows: DirectoryProperty

    init {
        typedKotlinFlows.convention(project.layout.buildDirectory.dir("cikraft/typedSteps/kotlin"))
    }

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateTypedKotlinStepBuilderTaskWorker::class) {
            jsonScriptEntry.from(this@GenerateTypedKotlinStepBuilderTask.jsonScriptEntry)
            typedKotlinFlows.set(this@GenerateTypedKotlinStepBuilderTask.typedKotlinFlows)
        }
    }
}

abstract class GenerateTypedKotlinStepBuilderTaskWorker :
    WorkAction<GenerateTypedKotlinStepBuilderTaskWorker.WorkParameters> {
    interface WorkParameters : org.gradle.workers.WorkParameters {
        val jsonScriptEntry: ConfigurableFileCollection
        val typedKotlinFlows: DirectoryProperty
    }

    @ExperimentalKotlinPoetApi
    override fun execute() {
        val jsonScriptEntryFile = parameters.jsonScriptEntry.files
        val typedKotlinFlows = parameters.typedKotlinFlows.asFile.get()

        val entryPoints = jsonScriptEntryFile.flatMap {
            if (it.exists()) {
                Json.decodeFromString(
                    ListSerializer(Script.serializer()),
                    it.readText(),
                )
            } else {
                emptyList()
            }
        }

        for (file in generateTypedKotlinStepBuilder(entryPoints = entryPoints)) {
            file.writeTo(typedKotlinFlows)
        }
    }
}
