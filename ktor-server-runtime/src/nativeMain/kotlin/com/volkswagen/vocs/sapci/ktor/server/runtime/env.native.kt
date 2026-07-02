package app.softwork.cikraft.ktor.server.runtime

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
public actual fun env(name: String): String? {
    return getenv(name)?.toKString()
}
