package app.softwork.cikraft.proxy.builder

import app.softwork.cikraft.proxy.ApiProxyBuilder
import app.softwork.cikraft.proxy.ApiState
import app.softwork.cikraft.proxy.ServiceCode

public interface ApiProxiesBuilder {
    public fun apiProxy(
        name: String,
        title: String,
        description: String? = null,
        isVersioned: Boolean = false,
        serviceCode: ServiceCode = ServiceCode.REST,
        apiState: ApiState = ApiState.Active,
        builder: ApiProxyBuilder.() -> Unit,
    )
}
