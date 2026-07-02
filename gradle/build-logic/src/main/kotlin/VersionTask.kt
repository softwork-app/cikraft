import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

@CacheableTask
abstract class VersionTask : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val ktorCio: Property<String>

    @get:Input
    abstract val ktorLogging: Property<String>

    @get:Input
    abstract val sapCIScriptApi: Property<String>

    @get:Input
    abstract val sapCIGeneric: Property<String>

    @get:Input
    abstract val sapCIAdapter: Property<String>

    @get:Input
    abstract val camel: Property<String>

    @get:Input
    abstract val activation: Property<String>

    @get:Input
    abstract val groovy: Property<String>

    init {
        version.convention(project.version.toString())
    }

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        outputFolder.convention(project.layout.buildDirectory.dir("generated/version"))
    }

    @TaskAction
    fun generate() {
        File(outputFolder.asFile.get(), "version.kt").writeText(
            """
            |package  app.softwork.cikraft.gradle
            |
            |internal val VERSION: String = "${version.get()}"
            |
            |internal val KTOR_CIO: String = "${ktorCio.get()}"
            |
            |internal val KTOR_LOGGING: String = "${ktorLogging.get()}"
            |
            |internal val SAPCI_SCRIPT_API: String = "${sapCIScriptApi.get()}"
            |
            |internal val SAPCI_GENERIC_API: String = "${sapCIGeneric.get()}"
            |
            |internal val SAPCI_ADAPTER: String = "${sapCIAdapter.get()}"
            |
            |internal val SAPCI_CAMEL: String = "${camel.get()}"
            |
            |internal val SAPCI_ACTIVATION: String = "${activation.get()}"
            |
            |internal val SAPCI_GROOVY: String = "${groovy.get()}"
            |
            """.trimMargin(),
        )
    }
}
