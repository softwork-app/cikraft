import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

interface test : CreatedFlowConfig

context(config: test)
public fun StepBuilder.test(
    a: (Stage) -> String,
    b: (Stage) -> Int,
    d: (Stage) -> String,
    e: (Stage) -> String,
) {
  config.parameters["a"] = a
  config.parameters["b"] = b
  config.parameters["d"] = d
  config.parameters["e"] = e
  contentModifier("Properties for test") {
    externalParameter("a")
    externalParameter("b")
    externalParameter("d")
    externalParameter("e")
  }
  config.allowedHeaders.add("CCC")
  groovyScript(name = "test", function = "test", file = "entrypoints.groovy")
}
