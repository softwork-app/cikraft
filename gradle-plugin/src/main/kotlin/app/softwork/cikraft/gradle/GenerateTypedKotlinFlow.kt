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
public abstract class GenerateTypedKotlinFlow : DefaultTask() {
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
        typedKotlinFlows.convention(project.layout.buildDirectory.dir("cikraft/typedFlows/kotlin"))
    }

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val packageDescription: Property<String>

    @get:Input
    abstract val flowName: Property<String>

    @get:Input
    @get:Optional
    abstract val flowDescription: Property<String>

    @get:Input
    @get:Optional
    abstract val flowSource: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val flowTarget: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val suffix: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val groovyScripts: ConfigurableFileCollection

    @get:Input
    abstract val baseUrl: Property<String>

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateTypedFlowsWorker::class) {
            packageName.set(this@GenerateTypedKotlinFlow.packageName)
            packageDescription.set(this@GenerateTypedKotlinFlow.packageDescription)
            flowName.set(this@GenerateTypedKotlinFlow.flowName)
            flowDescription.set(this@GenerateTypedKotlinFlow.flowDescription)
            flowSource.addAll(this@GenerateTypedKotlinFlow.flowSource)
            flowTarget.addAll(this@GenerateTypedKotlinFlow.flowTarget)
            jsonScriptEntry.from(this@GenerateTypedKotlinFlow.jsonScriptEntry)
            typedKotlinFlows.set(this@GenerateTypedKotlinFlow.typedKotlinFlows)
            suffix.set(this@GenerateTypedKotlinFlow.suffix)
            baseUrl.set(this@GenerateTypedKotlinFlow.baseUrl)
            groovyScripts.from(this@GenerateTypedKotlinFlow.groovyScripts)
        }
    }
}

abstract class GenerateTypedFlowsWorker : WorkAction<GenerateTypedFlowsWorker.WorkParameters> {
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

        for (file in generateTypedKotlinFlows(
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
        )) {
            file.writeTo(typedKotlinFlows)
        }
    }
}
