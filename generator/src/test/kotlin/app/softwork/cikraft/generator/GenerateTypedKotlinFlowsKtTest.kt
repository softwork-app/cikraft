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
class GenerateTypedKotlinFlowsKtTest {
    private val kotlinDir get() = Path("src/testFixtures") / "kotlin"

    @Test
    fun validTypedKotlinFlow() {
        val expectedIFlow = (kotlinDir / "ImpSkAv01.kt").readText().drop(63)
        val expectedFoo = (kotlinDir / "foo.kt").readText()
        val customGroovyScript = (kotlinDir / "customGroovyScript.kt").readText()

        assertEquals(
            expected = listOf(
                expectedIFlow,
                expectedFoo,
                customGroovyScript,
            ),
            actual = generateTypedKotlinFlows(
                "Com_Example_Ktor_Resources",
                "FOO",
                "Baz_A",
                "Foo Bar API",
                flowSource = emptyList(),
                flowTarget = emptyList(),
                entryPoints = listOf(fooScript),
                suffixID = null,
                baseUrl = "foo",
                groovyScripts = listOf("custom"),
            ).map { it.toString() },
        )
    }

    @Test
    fun validTypedKotlinFlowWithPr() {
        val expected = kotlinDir / "pr" / "ImpSkAv02WithPr.kt"
        val expectedFoo = kotlinDir / "pr" / "fooPr.kt"

        assertEquals(
            expected = listOf(expected.readText().drop(88), expectedFoo.readText().drop(25)),
            actual = generateTypedKotlinFlows(
                "Com_Example_Ktor_Resources",
                "FOO",
                "Baz_A",
                "Foo Bar API",
                flowSource = listOf("Foo"),
                flowTarget = listOf("Bar"),
                entryPoints = listOf(fooScript),
                suffixID = "PR42",
                baseUrl = "foo",
                groovyScripts = listOf(),
            ).map { it.toString() },
        )
    }

    @Test
    fun twoEntryPoints() {
        val expectedIFlow = (kotlinDir / "BazTwo.kt").readText().drop(63)
        val expectedSetup = (kotlinDir / "setup.kt").readText()
        val expectedTwoPart1 = (kotlinDir / "twoPart1.kt").readText()
        val expectedDummy = (kotlinDir / "dummy.kt").readText()
        val expectedDummyWithOutput = (kotlinDir / "dummyWithOutput.kt").readText()
        val expectedTwoPart2 = (kotlinDir / "twoPart2.kt").readText()

        assertEquals(
            expected = listOf(
                expectedIFlow,
                expectedDummyWithOutput,
                expectedTwoPart1,
                expectedDummy,
                expectedTwoPart2,
                expectedSetup,
            ),
            actual = generateTypedKotlinFlows(
                packageName = "Com_Example_Ktor_Resources",
                packageDescription = "Test",
                flowName = "Baz_Two",
                flowDescription = "Foo Two API",
                flowSource = emptyList(),
                flowTarget = emptyList(),
                entryPoints = listOf(dummyWithOutputScript, twoPart1Script, dummyScript, twoPart2Script, setupScript),
                suffixID = null,
                baseUrl = "foo",
                groovyScripts = listOf(),
            ).map { it.toString() },
        )
    }
}
