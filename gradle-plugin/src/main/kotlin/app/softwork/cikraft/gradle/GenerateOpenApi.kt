package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import app.softwork.cikraft.generator.*
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val createdFlows: ConfigurableFileCollection

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

    @TaskAction
    internal fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath, transformers)
        }.submit(GenerateOpenApiWorker::class) {
            this.createdFlows.setFrom(this@GenerateOpenApi.createdFlows.asFileTree)
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
        }
    }

    internal abstract class GenerateOpenApiWorker : WorkAction<GenerateOpenApiWorker.Parameters> {
        interface Parameters : WorkParameters {
            val createdFlows: ConfigurableFileCollection
            val openApiFile: RegularFileProperty
            val name: Property<String>
            val description: Property<String>
            val version: Property<String>
            val servers: SetProperty<Pair<String, String?>>
        }

        override fun execute() {
            val json = Json {
                prettyPrint = true
            }

            val transformers = ServiceLoader.load(SAPOpenAPITransformer::class.java).toList()

            val createdFlows = parameters.createdFlows.map {
                json.decodeFromString(
                    CreatedFlow.serializer(),
                    it.readText(),
                )
            }

            val openApi = generateOpenApi(
                infrastructure = OpenApiInfrastructure(
                    apis = createdFlows,
                    name = parameters.name.get(),
                    description = parameters.description.orNull,
                    version = parameters.version.get(),
                    servers = parameters.servers.get().associate {
                        it.first to it.second
                    },
                    tags = createdFlows.associate {
                        it.packageName to it.packageDescription
                    },
                ),
                transformers,
            )

            parameters.openApiFile.asFile.get().writeText(json.encodeToString(openApi) + "\n")
        }
    }
}
