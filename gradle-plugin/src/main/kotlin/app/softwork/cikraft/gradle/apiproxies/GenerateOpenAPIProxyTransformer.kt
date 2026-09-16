package app.softwork.cikraft.gradle.apiproxies

import app.softwork.cikraft.gradle.GenerateOpenAPIProxyTransformerWorker
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class GenerateOpenAPIProxyTransformer : DefaultTask() {
    @get:Input
    internal abstract val httpSuffix: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Classpath
    abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateOpenAPIProxyTransformerWorker::class.java) {
            this.httpSuffix.set(this@GenerateOpenAPIProxyTransformer.httpSuffix)
            outputDirectory.set(this@GenerateOpenAPIProxyTransformer.outputDirectory)
        }
    }
}
