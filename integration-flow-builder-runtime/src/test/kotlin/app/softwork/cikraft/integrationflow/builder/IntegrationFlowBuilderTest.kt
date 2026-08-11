package app.softwork.cikraft.integrationflow.builder

import BazA
import exceptionScript
import foo
import fooExceptionFlow
import fooExceptionScript
import handleException
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationFlowBuilderTest {
    @Test
    fun build() {
        val builder = IntegrationFlowBuilder(
            scripts = listOf(fooExceptionScript, exceptionScript),
            outputFolder = null,
            stageNames = setOf("QS"),
        )

        builder.createPackageAndFlow(
            packageId = "ComExampleKtorResources",
            packageName = "Com_Example_Ktor_Resources",
            packageDescription = "",
            integrationFlowId = "ExceptionSubprocess",
            integrationFlowIdRaw = "ExceptionSubprocess",
            integrationFlowName = "ExceptionSubprocess",
            integrationFlowNameRaw = "ExceptionSubprocess",
            integrationFlowDescription = "Foo Bar API",
            integrationFlowSource = emptyList(),
            integrationFlowTarget = emptyList(),
            config = BazA,
            builder = {
                context(BazA) {
                    https(
                        "/foo/bar/baz",
                        userRole = "Foo",
                        xsrfProtection = true,
                    ) {
                        startMessage()
                        foo(
                            c = {
                                ""
                            },
                            d = { "" },
                            e = { 42 },
                            ds = { "" },
                            injected = {
                            },
                        )
                        endMessage()

                        exceptionSubprocess {
                            startErrorEvent()
                            handleException()
                            errorEndEvent()
                        }
                    }
                }
            },
        )

        assertEquals(listOf(fooExceptionFlow), builder.createdFlows)
    }
}
