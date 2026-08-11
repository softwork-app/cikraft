import app.softwork.cikraft.integrationflow.StepBuilder

context(config: foo)
public fun StepBuilder.handleException() {
    groovyScript(name = "handleException", function = "handleException", file = "entrypoints.groovy")
}
