package app.softwork.cikraft.gradle.apiproxies

import app.softwork.cikraft.gradle.GenerateOpenAPIProxyTransformerWorker
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
public abstract class GenerateOpenAPIProxyTransformer : DefaultTask() {
    @get:Nested
    public abstract val apiProxies: NamedDomainObjectContainer<Proxies>

    @get:Input
    public abstract val httpSuffix: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Classpath
    public abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            it.classpath.from(workerClasspath)
        }.submit(GenerateOpenAPIProxyTransformerWorker::class.java) {
            for (apiProxy in this@GenerateOpenAPIProxyTransformer.apiProxies) {
                it.apiProxies.put(apiProxy.name, apiProxy.apiHosts.get() to apiProxy.clientAuthEnabled.get())
            }
            it.httpSuffix.set(this@GenerateOpenAPIProxyTransformer.httpSuffix)
            it.outputDirectory.set(this@GenerateOpenAPIProxyTransformer.outputDirectory)
        }
    }
}

public abstract class Proxies @Inject constructor(private val myName: String) : Named {
    @Input
    override fun getName(): String = myName

    @get:Input
    public abstract val apiHosts: SetProperty<String>

    @get:Input
    public abstract val clientAuthEnabled: Property<Boolean>
}
