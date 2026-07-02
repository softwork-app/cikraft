package app.softwork.cikraft.core

public data class OpenApiInfrastructure(
    val apis: List<CreatedFlow>,
    val name: String,
    val description: String?,
    val version: String,
    val packages: Set<String>,
    val tags: Map<String, String>,
    val servers: Map<String, String?>,
)
