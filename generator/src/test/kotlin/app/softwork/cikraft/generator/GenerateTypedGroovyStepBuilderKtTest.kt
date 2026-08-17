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
class GenerateTypedGroovyStepBuilderKtTest {
    private val kotlinDir get() = Path("src/testFixtures") / "kotlin"

    @Test
    fun validTypedKotlinFlow() {
        val customGroovyScript = (kotlinDir / "customGroovyScript.kt").readText()

        assertEquals(
            expected = listOf(
                customGroovyScript,
            ),
            actual = generateTypedGroovyStepBuilder(
                groovyScripts = listOf("custom"),
            ).map { it.toString() },
        )
    }
}
