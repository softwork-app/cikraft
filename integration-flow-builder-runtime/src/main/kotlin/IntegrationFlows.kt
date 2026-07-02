import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

public fun IntegrationFlowBuilder.integrationFlows(): Unit = error(
"""This function should not be called.
Instead, you should create a file integrationFlows.kt without a package with this signature:
public fun IntegrationFlowBuilder.integrationFlows(): Unit""",
)
