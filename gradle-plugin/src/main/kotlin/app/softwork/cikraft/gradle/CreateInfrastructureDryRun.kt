package app.softwork.cikraft.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
public abstract class CreateInfrastructureDryRun : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val entryPoints: ConfigurableFileCollection

    @get:Input
    public abstract val expectedRawIFlowsIds: SetProperty<String>

    @get:OutputDirectory
    public abstract val outputFolder: DirectoryProperty

    init {
        group = "cikraft"
    }

    @get:Inject
    protected abstract val javaToolchainService: JavaToolchainService

    @get:Nested
    public abstract val launcher: Property<JavaLauncher>

    init {
        val toolchain = project.extensions.getByType(JavaPluginExtension::class.java).toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @get:Input
    internal abstract val stageNames: SetProperty<String>

    @get:Classpath
    public abstract val classpath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun create() {
        workerExecutor.processIsolation {
            it.classpath.from(classpath)
            it.forkOptions.setExecutable(launcher.map { it.executablePath.asFile }.get())
        }.submit(CreateInfrastructureDryRunWorker::class.java) {
            it.expectedRawIFlowsIds.set(expectedRawIFlowsIds)
            it.entryPoints.from(entryPoints)
            it.outputFolder.set(outputFolder)
            it.stageNames.set(stageNames)
        }
    }
}
