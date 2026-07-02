import app.softwork.cikraft.proxy.builder.ApiProxiesBuilder

public fun ApiProxiesBuilder.apiProxies(baseUrl: String, suffix: String): Unit = error(
"""This function should not be called.
Instead, you should create a file apiProxies.kt without a package with this signature:
public fun ApiProxiesBuilder.apiProxies(baseUrl: String, suffix: String): Unit""",
)
