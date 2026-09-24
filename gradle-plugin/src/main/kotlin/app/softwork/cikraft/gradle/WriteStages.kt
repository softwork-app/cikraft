package app.softwork.cikraft.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class WriteStages : DefaultTask() {
    @get:Input
    abstract val runtimeStages: MapProperty<String, String>

    @get:Input
    abstract val virtualApiHosts: MapProperty<String, Map<String, String>>

    @get:Input
    abstract val httpSuffix: Property<String>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    internal fun writeStage() {
        val httpSuffix = httpSuffix.get()

        val runtimeStages = runtimeStages.get().map { (stage, httpUrl) ->
            "${stage}_HTTP=$httpUrl" + httpSuffix
        }.joinToString(separator = "\n", postfix = "\n")

        val virtualApiHosts = virtualApiHosts.get().map { (stage, httpUrl) ->
            httpUrl.map { (apiVirtualHostName, httpUrl) ->
                "${stage}_API_$apiVirtualHostName=$httpUrl" + httpSuffix
            }.joinToString(separator = "\n")
        }.joinToString(separator = "\n", postfix = "\n")

        output.get().asFile.writeText(
            runtimeStages + virtualApiHosts,
        )
    }
}
