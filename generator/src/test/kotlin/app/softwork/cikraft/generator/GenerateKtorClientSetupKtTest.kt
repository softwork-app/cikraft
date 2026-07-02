package app.softwork.cikraft.generator

import fooFlow
import noOutputsFlow
import kotlin.io.path.*
import kotlin.test.*

class GenerateKtorClientSetupKtTest {
    @Test
    fun generate() {
        val clientSetupFile = generateKtorClientSetup(
            createdFlow = fooFlow,
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin" / "com/example/ktor/resources/setupBazAClient.kt").readText(),
            clientSetupFile?.toString(),
        )
    }

    @Test
    fun noOutputs() {
        val clientSetupFile = generateKtorClientSetup(
            createdFlow = noOutputsFlow,
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin" / "com/example/ktor/resources/setupBazNoOutputsClient.kt").readText(),
            clientSetupFile?.toString(),
        )
    }
}
