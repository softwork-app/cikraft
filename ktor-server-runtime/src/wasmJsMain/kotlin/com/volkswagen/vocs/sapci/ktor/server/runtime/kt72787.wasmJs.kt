package app.softwork.cikraft.ktor.server.runtime

@OptIn(ExperimentalWasmJsInterop::class)
public actual fun env(name: String): String? = js("process.env[name]")
