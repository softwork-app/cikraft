plugins {
    id("app.softwork.cikraft.openapi")
}

dependencies {
    cikraftOpenAPI(projects.api)
}

abstract class MyCustomTask: DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val openAPIFiles: ConfigurableFileCollection

    @TaskAction
    fun run() {}
}

val myCustomTask = tasks.register("myCustomTask", MyCustomTask::class) {
    openAPIFiles.from(configurations.cikraftOpenAPIFiles)
}
