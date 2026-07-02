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
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class CreateInfrastructureDryRun : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val entryPoints: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        group = "cikraft"
    }

    @get:Inject
    internal abstract val javaToolchainService: JavaToolchainService

    @get:Nested
    abstract val launcher: Property<JavaLauncher>

    init {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @get:Input
    internal abstract val stageNames: SetProperty<String>

    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    internal fun create() {
        val s = launcher.map { it.executablePath.asFile }.get()
        val ss = this@CreateInfrastructureDryRun.classpath
        workerExecutor.processIsolation {
            classpath.from(ss)
            forkOptions.setExecutable(s)
        }.submit(CreateInfrastructureDryRunWorker::class) {
            entryPoints.from(this@CreateInfrastructureDryRun.entryPoints)
            outputFolder.set(this@CreateInfrastructureDryRun.outputFolder)
            stageNames.set(this@CreateInfrastructureDryRun.stageNames)
        }
    }
}
