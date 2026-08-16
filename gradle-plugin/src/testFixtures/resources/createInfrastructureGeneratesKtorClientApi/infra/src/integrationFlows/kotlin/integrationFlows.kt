import Stage
import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder
import userRole

public fun IntegrationFlowBuilder.integrationFlows() {
  IF_Ba {
    https(
      url = "/foo/bar/baz",
      userRole = userRole,
    ) {
      startMessage()
      test(
        a = { when (it)  {
          Stage.Dev -> "a"
          Stage.Prd -> "b"
        }},
        b = { 42 },
        d = { "foo" },
        e = { "foo" },
      )
      endMessage()
    }
  }
}
