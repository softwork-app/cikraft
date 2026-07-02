package app.softwork.cikraft.generator

import app.softwork.cikraft.core.Value
import fooFlow
import noOutputsFlow
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratePropertiesTest {
    @Test
    fun single() {
        val generated = generateProperties(
            createdFlow = fooFlow,
            parameters = mapOf("e" to Value.INT(0)),
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/BazAConfig.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun noOutputs() {
        val generated = generateProperties(
            createdFlow = noOutputsFlow,
            parameters = mapOf("ee" to Value.INT(0)),
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/BazNoOutputsConfig.kt").readText(),
            generated.toString(),
        )
    }
}
