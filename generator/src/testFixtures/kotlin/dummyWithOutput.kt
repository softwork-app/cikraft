import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

public interface dummyWithOutput : CreatedFlowConfig

context(config: dummyWithOutput)
public fun StepBuilder.dummyWithOutput() {
  config.allowedHeaders.add("B")
  groovyScript(name = "dummyWithOutput", function = "dummyWithOutput", file = "entrypoints.groovy")
}
