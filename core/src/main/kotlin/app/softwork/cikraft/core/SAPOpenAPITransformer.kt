package app.softwork.cikraft.core

import io.github.hfhbd.kfx.openapi.model.OpenApi

public fun interface SAPOpenAPITransformer {
    public fun convert(openApi: OpenApi, infrastructure: OpenApiInfrastructure): OpenApi
}
