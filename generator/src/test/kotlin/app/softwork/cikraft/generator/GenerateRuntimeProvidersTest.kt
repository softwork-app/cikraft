package app.softwork.cikraft.generator

import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateRuntimeProvidersTest {
    @Test
    fun generateRuntimeProvidersTest() {
        val fileSpec = generateRuntimeProviders(
            mapOf(
                "FOO" to "foo",
                "BAR" to null,
            ),
        )

        assertEquals(
            // language=kotlin
            """import kotlin.String

/**
 * foo
 */
public const val FOO: String = "foo"

public const val BAR: String = null
""",
            fileSpec.toString(),
        )
    }
}
