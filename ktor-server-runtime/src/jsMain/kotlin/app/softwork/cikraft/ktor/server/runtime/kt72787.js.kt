package app.softwork.cikraft.ktor.server.runtime

public actual fun env(name: String): String? = process.env[name]

@JsModule("node:process")
@JsNonModule
internal external val process: dynamic
