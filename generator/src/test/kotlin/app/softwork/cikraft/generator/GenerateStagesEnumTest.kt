package app.softwork.cikraft.generator

import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateStagesEnumTest {
    @Test
    fun generateStagesEnum() {
        assertEquals(
            (Path("src/testFixtures") / "kotlin" / "Stage.kt").readText(),
            generateStagesEnum(
                mapOf(
                    "Dev" to EnumInput(
                        description = null,
                        httpServer = "https://dev",
                        web = "https://dev.home",
                        apiHttpServer = "https://dev.api",
                    ),
                    "Prd" to EnumInput(
                        description = "Prod doc",
                        httpServer = "https://prd",
                        web = "https://prd.home",
                        apiHttpServer = null,
                    ),
                ),
            ).toString(),
        )
    }
}
