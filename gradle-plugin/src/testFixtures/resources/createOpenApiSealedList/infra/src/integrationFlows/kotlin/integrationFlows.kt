import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Ba {
    https(
      url = "/foo/bar/baz",
      userRole = "SomeRole.send",
      xsrfProtection = true,
    ) {
      startMessage()
        sealed()
      endMessage()
    }
  }
}
