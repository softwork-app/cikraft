import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

public interface twoPart2 : CreatedFlowConfig

context(config: twoPart2)
public fun StepBuilder.twoPart2() {
  config.allowedHeaders.add("B")
  groovyScript(name = "twoPart2", function = "twoPart2", file = "entrypoints.groovy")
}
