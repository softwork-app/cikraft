package app.softwork.cikraft.generator

import dataStoreFlow
import fooFlow
import javaStreamFlow
import kotlinxIoFlow
import noOutputsFlow
import nullableFlow
import scriptWithoutBodyAfterScriptWithBodyFlow
import twoFlow
import kotlin.io.path.*
import kotlin.test.*

class GenerateFunctionKtTest {
    @Test
    fun single() {
        val generated = generateFunction(
            createdFlow = fooFlow,
        )
        assertEquals(
            Path("src/testFixtures/kotlin/com/example/ktor/resources/function.kt").readText().drop(63),
            generated.toString(),
        )
    }

    @Test
    fun two() {
        val generated = generateFunction(
            createdFlow = twoFlow,
        )
        assertEquals(
            Path("src/testFixtures/kotlin/com/example/ktor/resources/functionTwoPart.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun noOutputs() {
        val generated = generateFunction(
            createdFlow = noOutputsFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/BazNoOutputs.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun noOutputsWithRaw() {
        val generated = generateFunction(
            createdFlow = scriptWithoutBodyAfterScriptWithBodyFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/BazNoOutputsWithRaw.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun streams() {
        val generated = generateFunction(
            createdFlow = javaStreamFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/BazStream.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun kotlinxIO() {
        val generated = generateFunction(
            createdFlow = kotlinxIoFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/functionBazKotlinxIO.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun datastore() {
        val generated = generateFunction(
            createdFlow = dataStoreFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/dataStore.kt").readText(),
            generated.toString(),
        )
    }

    @Test
    fun nullableReturn() {
        val generated = generateFunction(
            createdFlow = nullableFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/nullable.kt").readText(),
            generated.toString(),
        )
    }
}
