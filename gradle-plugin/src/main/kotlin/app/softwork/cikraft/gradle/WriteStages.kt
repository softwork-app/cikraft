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
    abstract val stagesUrls: MapProperty<String, String>

    @get:Input
    abstract val httpSuffix: Property<String>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    internal fun writeStage() {
        val httpSuffix = httpSuffix.get()
        output.get().asFile.writeText(
            stagesUrls.get().toList().joinToString(separator = "\n", postfix = "\n") { (stage, httpUrl) ->
                "$stage=${httpUrl + httpSuffix}"
            },
        )
    }
}
