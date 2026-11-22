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
public abstract class GenerateGroovyEntryPoints : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val scripts: ConfigurableFileCollection

    @get:OutputFile
    public abstract val groovyEntryPoints: RegularFileProperty

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun generateGroovyEntrypoints() {
        workerExecutor.classLoaderIsolation {
            it.classpath.from(workerClasspath)
        }.submit(GenerateGroovyEntryPointsWorker::class.java) {
            it.scripts.from(scripts)
            it.groovyEntryPoints.set(groovyEntryPoints)
        }
    }
}

internal abstract class GenerateGroovyEntryPointsWorker : WorkAction<GenerateGroovyEntryPointsWorker.WorkParameters> {
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
