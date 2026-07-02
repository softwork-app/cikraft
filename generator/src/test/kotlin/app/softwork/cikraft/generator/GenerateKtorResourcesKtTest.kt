package app.softwork.cikraft.generator

import fooFlow
import kotlin.io.path.*
import kotlin.test.*

class GenerateKtorResourcesKtTest {
    @Test
    fun generate() {
        val resourcesFile = generateKtorResources(fooFlow)

        assertEquals(
            (Path("src/testFixtures") / "kotlin" / "com/example/ktor/resources/ktorResources.kt").readText(),
            resourcesFile?.toString(),
        )
    }
}
