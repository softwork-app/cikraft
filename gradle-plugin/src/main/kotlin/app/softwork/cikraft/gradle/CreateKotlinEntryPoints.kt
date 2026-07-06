package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.generator.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
public abstract class CreateKotlinEntryPoints : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val scripts: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val useAndroidxAnnotation: Property<Boolean>

    @get:OutputDirectory
    public abstract val kotlinEntryPoints: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generateGroovyEntrypoints() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(CreateKotlinEntryPointsAction::class) {
            scripts.from(this@CreateKotlinEntryPoints.scripts)
            kotlinEntryPointsDir.set(this@CreateKotlinEntryPoints.kotlinEntryPoints)
            useAndroidxAnnotation.set(this@CreateKotlinEntryPoints.useAndroidxAnnotation)
        }
    }
}

internal abstract class CreateKotlinEntryPointsAction : WorkAction<CreateKotlinEntryPointsAction.WorkParameters> {
    interface WorkParameters : org.gradle.workers.WorkParameters {
        val scripts: ConfigurableFileCollection
        val kotlinEntryPointsDir: DirectoryProperty
        val useAndroidxAnnotation: Property<Boolean>
    }

    override fun execute() {
        val scripts = parameters.scripts.flatMap {
            Json.decodeFromString(ListSerializer(Script.serializer()), it.readText())
        }
        writeKotlinEntryPoints(
            scripts,
            parameters.useAndroidxAnnotation.orNull ?: false,
        ).writeTo(parameters.kotlinEntryPointsDir.asFile.get())
    }
}
