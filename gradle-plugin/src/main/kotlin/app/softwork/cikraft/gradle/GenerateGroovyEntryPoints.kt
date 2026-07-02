package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.generator.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateGroovyEntryPoints : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val scripts: ConfigurableFileCollection

    @get:OutputFile
    abstract val groovyEntryPoints: RegularFileProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generateGroovyEntrypoints() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateGroovyEntryPointsWorker::class) {
            scripts.from(this@GenerateGroovyEntryPoints.scripts)
            groovyEntryPoints.set(this@GenerateGroovyEntryPoints.groovyEntryPoints)
        }
    }
}

abstract class GenerateGroovyEntryPointsWorker : WorkAction<GenerateGroovyEntryPointsWorker.WorkParameters> {
    interface WorkParameters : org.gradle.workers.WorkParameters {
        val scripts: ConfigurableFileCollection
        val groovyEntryPoints: RegularFileProperty
    }

    override fun execute() {
        val scripts = parameters.scripts.flatMap {
            Json.decodeFromString(ListSerializer(Script.serializer()), it.readText())
        }
        val groovyEntryPointFile = parameters.groovyEntryPoints.asFile.get()

        val groovyEntryPoints = writeGroovyEntryPoints(scripts)
        groovyEntryPointFile.writeText(groovyEntryPoints)
    }
}
