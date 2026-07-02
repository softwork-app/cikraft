import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
    IF_Baz {
        https(
            url = "/foo/bar/baz",
            userRole = "SomeRole.send",
        ) {
            startMessage()
            raw()
            endMessage()
        }
    }
}
