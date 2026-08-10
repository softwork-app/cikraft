import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Bar {
    https(
      url = "/bar",
      userRole = "SomeRole.send",
      xsrfProtection = true,
    ) {
      startMessage()
      bar()
      endMessage()
    }
  }
}
