plugins {
    id("app.softwork.cikraft.openapi")
}

dependencies {
    ciKraftInfrastructure(projects.infra)
}

abstract class MyCustomTask: DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val openAPIFiles: ConfigurableFileCollection

    @TaskAction
    fun run() {}
}

val myCustomTask by tasks.registering(MyCustomTask::class) {
    openAPIFiles.from(configurations.ciKraftOpenAPI)
}
