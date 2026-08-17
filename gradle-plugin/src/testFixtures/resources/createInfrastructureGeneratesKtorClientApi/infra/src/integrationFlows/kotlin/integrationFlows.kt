import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder
import testWithDefaults
import userRole

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Ba {
    https(
      url = "/foo/bar/baz",
      userRole = userRole,
    ) {
      startMessage()
      testWithDefaults()
      endMessage()
    }
  }
}
