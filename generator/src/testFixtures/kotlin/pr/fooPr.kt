package pr

import Stage
import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig
import kotlin.Int
import kotlin.String
import kotlin.Unit

public interface foo : CreatedFlowConfig

/**
 * @param c Foo
 */
context(config: foo)
public fun StepBuilder.foo(
  c: (Stage) -> String,
  d: (Stage) -> String,
  e: ((Stage) -> Int)?,
  ds: (Stage) -> String,
  injected: StepBuilder.() -> Unit,
) {
  config.parameters["c"] = c
  config.parameters["d"] = d
  if (e != null) {
    config.parameters["e"] = e
  }
  config.parameters["ds"] = ds
  withPrefix("Injected_") {
    injected()
  }
  contentModifier("Set injected for foo") {
    property("injected", "_RESULT_")
  }
  contentModifier("Properties for foo") {
    externalParameter("c")
    externalParameter("d")
    if (e != null) {
      externalParameter("e")
    }
    externalParameter("ds")
  }
  config.allowedHeaders.add("B")
  groovyScript(name = "foo", function = "foo", file = "entrypoints.groovy")
}
