import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

public interface custom : CreatedFlowConfig

context(config: custom)
public fun StepBuilder.custom() {
  groovyScript(name = "custom", file = "custom.groovy")
}
