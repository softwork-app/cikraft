package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Value
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
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
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.StringWriter
import java.util.*
import javax.inject.Inject

@CacheableTask
abstract class WritePropertiesToFile : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val propertyFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(WritePropertiesToFileWorker::class) {
            output.set(this@WritePropertiesToFile.output)
            propertyFiles.from(this@WritePropertiesToFile.propertyFiles.asFileTree)
        }
    }
}

internal abstract class WritePropertiesToFileWorker : WorkAction<WritePropertiesToFileWorker.Parameters> {
    interface Parameters : WorkParameters {
        val output: RegularFileProperty
        val propertyFiles: ConfigurableFileCollection
    }

    override fun execute() {
        val output = parameters.output.get().asFile

        val properties = Properties()

        for (propertyFile in parameters.propertyFiles) {
            val iFlowName = propertyFile.nameWithoutExtension
            val values = propertyFile.readText().let {
                Json.decodeFromString(MapSerializer(String.serializer(), Value.serializer()), it)
            }
            for ((propertyName, value) in values) {
                val name = "${iFlowName}_$propertyName".uppercase()
                val stringValue = when (value) {
                    is Value.BOOLEAN -> value.value.toString()
                    is Value.DOUBLE -> value.value.toString()
                    is Value.FLOAT -> value.value.toString()
                    is Value.INT -> value.value.toString()
                    is Value.STRING -> value.value
                }
                properties[name] = stringValue
            }
        }
        val writer = StringWriter()
        properties.store(writer, null)
        output.writeText(writer.toString().lines().drop(1).joinToString(System.lineSeparator()))
    }
}
