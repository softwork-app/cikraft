package app.softwork.cikraft.generator

import com.example.*
import fooScript
import fooScriptNoError
import fooSuspendScript
import noOutputsScript
import rawScript
import rawSuspendScript
import kotlin.io.path.*
import kotlin.test.*

class GenerateGroovyEntryPointsKtTest {
    @Test
    fun validGroovyFile() {
        assertEquals(
            Path("src/testFixtures/groovy/entrypoints.groovy").readText(),
            writeGroovyEntryPoints(
                listOf(fooScript),
            ),
        )
    }

    @Test
    fun coroutines() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "entrypointsWithCoroutines.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    fooSuspendScript,
                ),
            ),
        )
    }

    @Test
    fun serialized() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "serializedBody.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    serializedScript,
                ),
            ),
        )
    }

    @Test
    fun typed() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "typed.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    typedScript,
                ),
            ),
        )
    }

    @Test
    fun noError() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "entrypointsNoError.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    fooScriptNoError,
                ),
            ),
        )
    }

    @Test
    fun raw() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "entrypointsRaw.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    rawScript,
                ),
            ),
        )
    }

    @Test
    fun rawSuspend() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "entrypointsRawWithCoroutines.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    rawSuspendScript,
                ),
            ),
        )
    }

    @Test
    fun noOutput() {
        assertEquals(
            (Path("src/testFixtures") / "groovy" / "entrypointsNoOutput.groovy").readText(),
            writeGroovyEntryPoints(
                scripts = listOf(
                    noOutputsScript,
                ),
            ),
        )
    }
}
