import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

interface setup : CreatedFlowConfig

context(config: setup)
public fun StepBuilder.setup(a: (Stage) -> String, b: (Stage) -> Int) {
  config.parameters["a"] = a
  config.parameters["b"] = b
  contentModifier("Properties for setup") {
    externalParameter("a")
    externalParameter("b")
  }
  groovyScript(name = "setup", function = "setup", file = "entrypoints.groovy")
}
