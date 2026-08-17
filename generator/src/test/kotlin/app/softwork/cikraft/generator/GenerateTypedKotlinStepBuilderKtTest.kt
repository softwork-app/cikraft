package app.softwork.cikraft.generator

import com.squareup.kotlinpoet.*
import dummyScript
import dummyWithOutputScript
import fooScript
import setupScript
import twoPart1Script
import twoPart2Script
import kotlin.io.path.*
import kotlin.test.*

@ExperimentalKotlinPoetApi
class GenerateTypedKotlinStepBuilderKtTest {
    private val kotlinDir get() = Path("src/testFixtures") / "kotlin"

    @Test
    fun validTypedKotlinFlow() {
        val expectedFoo = (kotlinDir / "foo.kt").readText()

        assertEquals(
            expected = listOf(
                expectedFoo,
            ),
            actual = generateTypedKotlinStepBuilder(
                entryPoints = listOf(fooScript),
            ).map { it.toString() },
        )
    }

    @Test
    fun validTypedKotlinFlowWithPr() {
        val expectedFoo = kotlinDir / "pr" / "fooPr.kt"

        assertEquals(
            expected = listOf(expectedFoo.readText().drop(25)),
            actual = generateTypedKotlinStepBuilder(
                entryPoints = listOf(fooScript),
            ).map { it.toString() },
        )
    }

    @Test
    fun twoEntryPoints() {
        val expectedSetup = (kotlinDir / "setup.kt").readText()
        val expectedTwoPart1 = (kotlinDir / "twoPart1.kt").readText()
        val expectedDummy = (kotlinDir / "dummy.kt").readText()
        val expectedDummyWithOutput = (kotlinDir / "dummyWithOutput.kt").readText()
        val expectedTwoPart2 = (kotlinDir / "twoPart2.kt").readText()

        assertEquals(
            expected = listOf(
                expectedDummyWithOutput,
                expectedTwoPart1,
                expectedDummy,
                expectedTwoPart2,
                expectedSetup,
            ),
            actual = generateTypedKotlinStepBuilder(
                entryPoints = listOf(dummyWithOutputScript, twoPart1Script, dummyScript, twoPart2Script, setupScript),
            ).map { it.toString() },
        )
    }
}
