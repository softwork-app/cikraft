import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

public interface dummy : CreatedFlowConfig

context(config: dummy)
public fun StepBuilder.dummy() {
  config.allowedHeaders.add("B")
  groovyScript(name = "dummy", function = "dummy", file = "entrypoints.groovy")
}
