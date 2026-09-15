package app.softwork.cikraft.gradle.apiproxies

import app.softwork.cikraft.generator.generateRuntimeProviders
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class GenerateTypeApiRuntimeProviders : DefaultTask() {
    @get:Nested
    abstract val apiRuntimeProviders: NamedDomainObjectCollection<ApiRuntimeProvider>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    interface ApiRuntimeProvider : Named {
        @Input
        override fun getName(): String

        @get:Input
        @get:Optional
        val description: Property<String>
    }

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateTypeApiRuntimeProvidersWorker::class.java) {
            apiRuntimeProviders.addAll(this@GenerateTypeApiRuntimeProviders.apiRuntimeProviders)
            outputDirectory.set(this@GenerateTypeApiRuntimeProviders.outputDirectory)
        }
    }
}

internal abstract class GenerateTypeApiRuntimeProvidersWorker :
    WorkAction<GenerateTypeApiRuntimeProvidersWorker.Parameters> {
    interface Parameters : WorkParameters {
        val apiRuntimeProviders: SetProperty<GenerateTypeApiRuntimeProviders.ApiRuntimeProvider>
        val outputDirectory: DirectoryProperty
    }

    override fun execute() {
        generateRuntimeProviders(
            parameters.apiRuntimeProviders.get().associate { it.name to it.description.orNull },
        ).writeTo(parameters.outputDirectory.get().asFile)
    }
}
