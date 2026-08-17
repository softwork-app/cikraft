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
class GenerateTypedKotlinIntegrationFlowBuilderKtTest {
    private val kotlinDir get() = Path("src/testFixtures") / "kotlin"

    @Test
    fun validTypedKotlinFlow() {
        val expectedIFlow = (kotlinDir / "ImpSkAv01.kt").readText().drop(63)

        assertEquals(
            expected = expectedIFlow,
            actual = generateTypedKotlinIntegrationFlowBuilder(
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
            ).toString(),
        )
    }

    @Test
    fun validTypedKotlinFlowWithPr() {
        val expected = kotlinDir / "pr" / "ImpSkAv02WithPr.kt"

        assertEquals(
            expected = expected.readText().drop(88),
            actual = generateTypedKotlinIntegrationFlowBuilder(
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
            ).toString(),
        )
    }

    @Test
    fun twoEntryPoints() {
        val expectedIFlow = (kotlinDir / "BazTwo.kt").readText().drop(63)

        assertEquals(
            expected = expectedIFlow,
            actual = generateTypedKotlinIntegrationFlowBuilder(
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
            ).toString(),
        )
    }
}
