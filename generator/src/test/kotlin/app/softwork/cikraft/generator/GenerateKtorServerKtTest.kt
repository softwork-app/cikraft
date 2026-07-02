package app.softwork.cikraft.generator

import dataStoreFlow
import fooFlow
import javaStreamFlow
import kotlinxIoFlow
import noOutputsFlow
import twoFlow
import kotlin.io.path.*
import kotlin.test.*

class GenerateKtorServerKtTest {
    @Test
    fun single() {
        val generated = generateKtorServer(
            createdFlow = fooFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/server.kt").readText().drop(63),
            generated?.toString(),
        )
    }

    @Test
    fun two() {
        val generated = generateKtorServer(
            createdFlow = twoFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/two.kt").readText().drop(63),
            generated?.toString(),
        )
    }

    @Test
    fun noOutputs() {
        val generated = generateKtorServer(
            createdFlow = noOutputsFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/noOutputs.kt").readText().drop(63),
            generated?.toString(),
        )
    }

    @Test
    fun streams() {
        val generated = generateKtorServer(
            createdFlow = javaStreamFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/javaStream.kt").readText().drop(63),
            generated?.toString(),
        )
    }

    @Test
    fun kotlinxIO() {
        val generated = generateKtorServer(
            createdFlow = kotlinxIoFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/kotlinxio.kt").readText().drop(63),
            generated?.toString(),
        )
    }

    @Test
    fun dataStore() {
        val generated = generateKtorServer(
            createdFlow = dataStoreFlow,
        )
        assertEquals(
            (Path("src/testFixtures") / "kotlin/com/example/ktor/resources/dataStoreKotlin.kt").readText().drop(63),
            generated?.toString(),
        )
    }
}
