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
            (Path("src/testFixtures") / "kotlin" / "CiKraftEntrypoints.kt").readText(),
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
                useAndroidxAnnotation = true,
            ).toString(),
        )
    }

    @Test
    fun generatesValidKotlinEntryPointsWithoutKeep() {
        assertEquals(
            """import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.msglog.MessageLog

public fun Message.injectedBoolean(messageLog: MessageLog): Message {
  val output = injectedBoolean()
  setProperty("_RESULT_", output)
  return this
}
""",
            writeKotlinEntryPoints(
                listOf(
                    injectedBooleanScript,
                ),
                useAndroidxAnnotation = false,
            ).toString(),
        )
    }

    @Test
    fun failsWithStarScript() {
        val exception = assertFailsWith<IllegalStateException> {
            writeKotlinEntryPoints(listOf(starScript), useAndroidxAnnotation = false)
        }
        assertEquals("Star types are not allowed", exception.message)
    }
}
