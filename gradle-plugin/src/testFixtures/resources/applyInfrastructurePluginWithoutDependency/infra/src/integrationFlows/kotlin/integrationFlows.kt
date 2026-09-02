import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
    IF_Bar {
        https(
            url = "/foo/bar/baz",
            userRole = "SomeRole.send",
            xsrfProtection = true,
        ) {
            startMessage()
            endMessage()
        }
    }
}
