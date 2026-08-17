package app.softwork.cikraft.gradle

import app.softwork.cikraft.generator.*
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
public abstract class GenerateTypedGroovyStepBuilderTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:OutputDirectory
    public abstract val typedKotlinFlows: DirectoryProperty

    init {
        typedKotlinFlows.convention(project.layout.buildDirectory.dir("cikraft/typedSteps/groovy"))
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val groovyScripts: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateTypedGroovyStepBuilderWorker::class) {
            typedKotlinFlows.set(this@GenerateTypedGroovyStepBuilderTask.typedKotlinFlows)
            groovyScripts.from(this@GenerateTypedGroovyStepBuilderTask.groovyScripts)
        }
    }
}

abstract class GenerateTypedGroovyStepBuilderWorker : WorkAction<GenerateTypedGroovyStepBuilderWorker.WorkParameters> {
    interface WorkParameters : org.gradle.workers.WorkParameters {
        val typedKotlinFlows: DirectoryProperty
        val groovyScripts: ConfigurableFileCollection
    }

    @ExperimentalKotlinPoetApi
    override fun execute() {
        val typedKotlinFlows = parameters.typedKotlinFlows.asFile.get()

        for (file in generateTypedGroovyStepBuilder(
            groovyScripts = parameters.groovyScripts.map { it.nameWithoutExtension },
        )) {
            file.writeTo(typedKotlinFlows)
        }
    }
}
