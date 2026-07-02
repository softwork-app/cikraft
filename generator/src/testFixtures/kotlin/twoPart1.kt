import app.softwork.cikraft.integrationflow.StepBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig
import kotlin.Unit

public interface twoPart1 : CreatedFlowConfig

context(config: twoPart1)
public fun StepBuilder.twoPart1(injected: StepBuilder.() -> Unit) {
  withPrefix("Injected_") {
    injected()
  }
  contentModifier("Set injected for twoPart1") {
    property("injected", "_RESULT_")
  }
  config.allowedHeaders.add("B")
  groovyScript(name = "twoPart1", function = "twoPart1", file = "entrypoints.groovy")
}
