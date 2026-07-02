import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Ba {
    https(
      url = "/foo/bar/baz",
      userRole = "SomeRole.send",
      xsrfProtection = true,
    ) {
      startMessage()
      test(
        a = { "a" },
        b = { 42 },
        d = { "foo" },
        e = { "foo" },
      )
      endMessage()
    }
  }
}
