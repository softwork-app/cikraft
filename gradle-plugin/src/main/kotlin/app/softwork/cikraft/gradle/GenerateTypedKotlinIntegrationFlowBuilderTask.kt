package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Script
import app.softwork.cikraft.generator.*
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
public abstract class GenerateTypedKotlinIntegrationFlowBuilderTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val jsonScriptEntry: ConfigurableFileCollection

    @get:OutputDirectory
    public abstract val typedKotlinFlows: DirectoryProperty

    init {
        typedKotlinFlows.convention(project.layout.buildDirectory.dir("cikraft/typedFlows/kotlin"))
    }

    @get:Input
    public abstract val packageName: Property<String>

    @get:Input
    public abstract val packageDescription: Property<String>

    @get:Input
    public abstract val flowName: Property<String>

    @get:Input
    @get:Optional
    public abstract val flowDescription: Property<String>

    @get:Input
    @get:Optional
    public abstract val flowSource: ListProperty<String>

    @get:Input
    @get:Optional
    public abstract val flowTarget: ListProperty<String>

    @get:Input
    @get:Optional
    public abstract val suffix: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract val groovyScripts: ConfigurableFileCollection

    @get:Input
    public abstract val baseUrl: Property<String>

    @get:Input
    @get:Optional
    public abstract val autoNumbering: Property<Boolean>

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            it.classpath.from(workerClasspath)
        }.submit(GenerateTypedFlowsWorker::class.java) {
            it.packageName.set(packageName)
            it.packageDescription.set(packageDescription)
            it.flowName.set(flowName)
            it.flowDescription.set(flowDescription)
            it.flowSource.addAll(flowSource)
            it.flowTarget.addAll(flowTarget)
            it.jsonScriptEntry.from(jsonScriptEntry)
            it.typedKotlinFlows.set(typedKotlinFlows)
            it.suffix.set(suffix)
            it.baseUrl.set(baseUrl)
            it.groovyScripts.from(groovyScripts)
            it.autoNumbering.set(autoNumbering)
        }
    }
}

internal abstract class GenerateTypedFlowsWorker : WorkAction<GenerateTypedFlowsWorker.WorkParameters> {
    interface WorkParameters : org.gradle.workers.WorkParameters {
        val packageName: Property<String>
        val packageDescription: Property<String>
        val flowName: Property<String>
        val flowDescription: Property<String>
        val flowSource: ListProperty<String>
        val flowTarget: ListProperty<String>
        val jsonScriptEntry: ConfigurableFileCollection
        val typedKotlinFlows: DirectoryProperty
        val suffix: Property<String>
        val baseUrl: Property<String>
        val groovyScripts: ConfigurableFileCollection
        val autoNumbering: Property<Boolean>
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

        generateTypedKotlinIntegrationFlowBuilder(
            packageName = parameters.packageName.get(),
            packageDescription = parameters.packageDescription.orNull,
            flowName = parameters.flowName.get(),
            flowDescription = parameters.flowDescription.orNull,
            flowSource = parameters.flowSource.get(),
            flowTarget = parameters.flowTarget.get(),
            entryPoints = entryPoints,
            suffixID = parameters.suffix.orNull?.takeIf { it.isNotBlank() }?.replace("/", "")?.uppercase(),
            baseUrl = parameters.baseUrl.get(),
            groovyScripts = parameters.groovyScripts.map { it.nameWithoutExtension },
            autoNumbering = parameters.autoNumbering.getOrElse(false),
        ).writeTo(typedKotlinFlows)
    }
}
