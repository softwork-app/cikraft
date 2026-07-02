package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import app.softwork.cikraft.generator.*
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.util.ServiceLoader
import javax.inject.Inject

interface Server {
    @get:Input
    val http: Property<String>

    @get:Input
    @get:Optional
    val description: Property<String>
}

@CacheableTask
abstract class GenerateOpenApi : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:OutputFile
    abstract val openApiFile: RegularFileProperty

    init {
        openApiFile.convention(project.layout.buildDirectory.file("cikraft/openapi.json"))
    }

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val createdFlows: DirectoryProperty

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val apiDescription: Property<String>

    @get:Input
    val version = project.version.toString()

    @get:Nested
    abstract val servers: ListProperty<Server>

    @get:Classpath
    abstract val transformers: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Input
    abstract val tags: MapProperty<String, String>

    @get:Input
    abstract val packages: SetProperty<String>

    @TaskAction
    internal fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath, transformers)
        }.submit(GenerateOpenApiWorker::class) {
            this.createdFlows.set(this@GenerateOpenApi.createdFlows)
            this.openApiFile.set(this@GenerateOpenApi.openApiFile)
            this.name.set(this@GenerateOpenApi.title)
            this.description.set(this@GenerateOpenApi.apiDescription)
            this.version.set(this@GenerateOpenApi.version)
            this.servers.set(
                this@GenerateOpenApi.servers.map {
                    it.map {
                        it.http.get() to it.description.orNull
                    }
                },
            )
            this.tags.set(this@GenerateOpenApi.tags)
            this.packages.set(this@GenerateOpenApi.packages)
        }
    }

    internal abstract class GenerateOpenApiWorker : WorkAction<GenerateOpenApiWorker.Parameters> {
        interface Parameters : WorkParameters {
            val createdFlows: DirectoryProperty
            val openApiFile: RegularFileProperty
            val name: Property<String>
            val description: Property<String>
            val version: Property<String>
            val servers: SetProperty<Pair<String, String?>>
            val tags: MapProperty<String, String>
            val packages: SetProperty<String>
        }

        override fun execute() {
            val json = Json {
                prettyPrint = true
            }

            val transformers = ServiceLoader.load(SAPOpenAPITransformer::class.java).toList()

            val openApi = generateOpenApi(
                infrastructure = OpenApiInfrastructure(
                    apis = parameters.createdFlows.get().asFile.listFiles().map {
                        json.decodeFromString(
                            CreatedFlow.serializer(),
                            it.readText(),
                        )
                    },
                    name = parameters.name.get(),
                    description = parameters.description.orNull,
                    version = parameters.version.get(),
                    servers = parameters.servers.get().associate {
                        it.first to it.second
                    },
                    tags = parameters.tags.get(),
                    packages = parameters.packages.get(),
                ),
                transformers,
            )

            parameters.openApiFile.asFile.get().writeText(json.encodeToString(openApi) + "\n")
        }
    }
}
