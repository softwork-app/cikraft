import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Multiple {
    https(
      url = "/foo/bar/baz",
      userRole = "SomeRole.send",
    ) {
      startMessage()
      setup(
        a = { "foo" },
        b = { 42 },
      )
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
