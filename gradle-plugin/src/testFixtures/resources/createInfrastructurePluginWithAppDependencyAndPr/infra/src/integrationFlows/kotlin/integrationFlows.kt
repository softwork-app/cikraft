import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows() {
    println("The Answer to the Ultimate Question of Life, the Universe, and Everything is 42")
    IF_Ba {
        https("", "SomeRole.send") {
            startMessage()
            test(
                a = { "" },
                b = { 42 },
                d = { "" },
                e = { "" },
            )
            endMessage()
        }
    }
}
