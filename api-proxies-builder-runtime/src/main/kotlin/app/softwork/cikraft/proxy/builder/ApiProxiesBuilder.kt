package app.softwork.cikraft.proxy.builder

import app.softwork.cikraft.proxy.ApiProxyBuilder

public interface ApiProxiesBuilder {
    public fun apiProxy(
        name: String,
        title: String,
        description: String? = null,
        builder: ApiProxyBuilder.() -> Unit,
    )
}
