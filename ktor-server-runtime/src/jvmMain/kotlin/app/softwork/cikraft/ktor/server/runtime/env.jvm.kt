package app.softwork.cikraft.ktor.server.runtime

public actual fun env(name: String): String? {
    return System.getenv(name)
}
