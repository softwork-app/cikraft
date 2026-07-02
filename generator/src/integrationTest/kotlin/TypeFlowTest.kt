import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeFlowTest {
    @Test
    fun injected() {
        val randomOrder = listOf(dummyWithOutputScript, twoPart1Script, twoPart2Script, dummyScript, setupScript)
        val builder = IntegrationFlowBuilder(
            scripts = randomOrder,
            null,
            stageNames = setOf("SBX")
        )

        builder.Baz_Two {
            https(
                url = "/foo/bar/two",
                userRole = "Foo",
                xsrfProtection = true,
            ) {
                startMessage()
                twoPart1(
                    injected = {
                        setup()
                    },
                )
                dummy()
                dummyWithOutput()
                twoPart2()
                write(
                    dataStoreName = "DS_DATA",
                    entryID = "myEntryId",
                )
                endMessage()
            }
        }

        assertEquals(expected = listOf(twoFlow), actual = builder.createdFlows)
    }
}
