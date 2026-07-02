package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.*
import app.softwork.cikraft.generator.*
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateKtorServerAPI : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val createdFlows: DirectoryProperty

    @get:OutputDirectory
    abstract val ktorApi: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        val worker = workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }
        for (createdFlow in createdFlows.get().asFile.listFiles()) {
            worker.submit(GenerateKtorServer::class) {
                this.createdFlow.set(createdFlow)
                this.outputDirectory.set(ktorApi)
            }
        }
    }

    internal abstract class GenerateKtorServer : WorkAction<GenerateKtorServer.Param> {
        interface Param : WorkParameters {
            val createdFlow: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            val createdFlowFile = parameters.createdFlow.asFile.get()
            val createdFlow = Json.decodeFromString(
                CreatedFlow.serializer(),
                createdFlowFile.readText(),
            )

            generateKtorServer(createdFlow)?.writeTo(parameters.outputDirectory.asFile.get())
        }
    }
}
