package app.softwork.cikraft.ktor.server.runtime

import kotlin.test.Test
import kotlin.test.assertNull

class EnvTest {
    @Test
    fun notAvailable() {
        assertNull(env("FOO"))
    }
}
