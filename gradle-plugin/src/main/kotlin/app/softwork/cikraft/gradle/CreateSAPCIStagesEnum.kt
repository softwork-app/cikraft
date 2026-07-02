package app.softwork.cikraft.gradle

import app.softwork.cikraft.generator.EnumInput
import app.softwork.cikraft.generator.generateStagesEnum
import app.softwork.cikraft.gradle.CreateSAPCIStagesEnumWorker.Params
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal interface EnumStage : Named {
    @Input
    override fun getName(): String

    @get:Input
    @get:Optional
    val stageDescription: Property<String>

    @get:Input
    val httpServer: Property<String>

    @get:Input
    @get:Optional
    val apiHttpServer: Property<String>

    @get:Input
    val web: Property<String>
}

@CacheableTask
abstract class CreateSAPCIStagesEnum : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:Classpath
    internal abstract val classpath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Nested
    internal abstract val stages: NamedDomainObjectContainer<EnumStage>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(project.layout.buildDirectory.dir("cikraft/generated/stages"))
    }

    @TaskAction
    internal fun execute() {
        workerExecutor.classLoaderIsolation {
            this.classpath.from(this@CreateSAPCIStagesEnum.classpath)
        }.submit(CreateSAPCIStagesEnumWorker::class) {
            this.stages.putAll(
                this@CreateSAPCIStagesEnum.stages.associateBy { it.name },
            )
            this.outputDir.set(this@CreateSAPCIStagesEnum.outputDir)
        }
    }
}

internal abstract class CreateSAPCIStagesEnumWorker : WorkAction<Params> {
    interface Params : org.gradle.workers.WorkParameters {
        val stages: MapProperty<String, EnumStage>
        val outputDir: DirectoryProperty
    }

    override fun execute() {
        val stageEnum = generateStagesEnum(
            stages = parameters.stages.get().mapValues { (_, it) ->
                EnumInput(
                    description = it.stageDescription.orNull,
                    httpServer = it.httpServer.get(),
                    web = it.web.get(),
                    apiHttpServer = it.apiHttpServer.orNull,
                )
            },
        )
        stageEnum.writeTo(parameters.outputDir.asFile.get())
    }
}
