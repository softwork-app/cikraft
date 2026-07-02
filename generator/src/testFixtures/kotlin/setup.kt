import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

public interface setup : CreatedFlowConfig

context(config: setup)
public fun StepBuilder.setup() {
  groovyScript(name = "setup", function = "setup", file = "entrypoints.groovy")
}
