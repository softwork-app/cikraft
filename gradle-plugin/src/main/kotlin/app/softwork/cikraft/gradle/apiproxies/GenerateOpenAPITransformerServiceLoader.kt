package app.softwork.cikraft.gradle.apiproxies

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File

@UntrackedTask(because = "Not worth")
abstract class GenerateOpenAPITransformerServiceLoader : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    protected fun generate() {
        val services = File(outputDirectory.get().asFile, "META-INF/services")
        val file = File(services, "app.softwork.cikraft.core.SAPOpenAPITransformer")
        file.parentFile.mkdirs()

        file.writeText("APIProxyOpenAPITransformer" + "\n")
    }
}
