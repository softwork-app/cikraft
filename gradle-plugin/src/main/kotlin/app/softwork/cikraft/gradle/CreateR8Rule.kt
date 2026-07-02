package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.generator.createR8Rule
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.NormalizeLineEndings
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class CreateR8Rule : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val scriptEntries: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun create() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(CreateR8RuleWorker::class) {
            this.scriptEntries.from(this@CreateR8Rule.scriptEntries)
            this.ruleFile.set(outputFile)
        }
    }
}

internal abstract class CreateR8RuleWorker : WorkAction<CreateR8RuleWorker.Parameters> {
    interface Parameters : org.gradle.workers.WorkParameters {
        val scriptEntries: ConfigurableFileCollection
        val ruleFile: RegularFileProperty
    }

    override fun execute() {
        val scripts = parameters.scriptEntries.files.flatMap {
            Json.decodeFromString(ListSerializer(Script.serializer()), it.readText())
        }

        parameters.ruleFile.get().asFile.writeText(createR8Rule(scripts))
    }
}
