package app.softwork.cikraft.generator

import binaryRedirectScript
import com.example.*
import fooScript
import fooScriptNoError
import fooSuspendScript
import javaStreamScript
import kotlinxIoScript
import noOutputsScript
import rawScript
import rawSuspendScript
import setupScript
import twoPart1Script
import twoPart2Script
import kotlin.io.path.*
import kotlin.test.*

class WriteKotlinEntryPointsKtTest {
    @Test
    fun generatesValidKotlinEntryPoints() {
        assertEquals(
            (Path("src/testFixtures") / "kotlin" / "Entrypoints.kt").readText(),
            writeKotlinEntryPoints(
                listOf(
                    fooScript,
                    fooSuspendScript,
                    serializedScript,
                    typedScript,
                    fooScriptNoError,
                    rawScript,
                    rawSuspendScript,
                    noOutputsScript,
                    setupScript,
                    twoPart1Script,
                    twoPart2Script,
                    javaStreamScript,
                    binaryRedirectScript,
                    kotlinxIoScript,
                    injectedBooleanScript,
                    nullableReturnScript,
                ),
            ).toString(),
        )
    }

    @Test
    fun failsWithStarScript() {
        val exception = assertFailsWith<IllegalStateException> {
            writeKotlinEntryPoints(listOf(starScript))
        }
        assertEquals("Star types are not allowed", exception.message)
    }
}
