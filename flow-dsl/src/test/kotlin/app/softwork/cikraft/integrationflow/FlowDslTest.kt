package app.softwork.cikraft.integrationflow

import kotlin.test.*

class FlowDslTest {
    data object Foo : Config {
        override val baseUrl: String = "/Foo"
        override val allowedHeaders: Set<String> = emptySet()
    }

    @Test
    fun single() {
        val generated = Foo.integrationFlow {
            https(
                url = "/01/50/4/1/7/5",
                userRole = "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send",
                xsrfProtection = true,
                clientCertificates = true,
                returnExceptionsToSender = true,
            ) {
                startMessage()
                contentModifier {
                    externalParameter("foo")
                    externalParameter("bar")
                }
                groovyScript(
                    function = "convert",
                    file = "action.groovy",
                )
                endMessage()
            }
        }
        assertEquals(expected = flow, actual = generated)
    }

    @Test
    fun multiple() {
        val generated = Foo.integrationFlow {
            https(
                url = "/01/50/4/1/7/5",
                userRole = "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send",
                xsrfProtection = true,
                clientCertificates = true,
                returnExceptionsToSender = true,
            ) {
                startMessage()
                groovyScript(
                    function = "convert1",
                    file = "action.groovy",
                )
                groovyScript(
                    function = "convert2",
                    file = "action.groovy",
                )
                groovyScript(
                    function = "convert3",
                    file = "action.groovy",
                )
                endMessage()
            }
        }
        assertEquals(expected = flowMultipleSteps, actual = generated)
    }

    @Test
    fun withExceptionHandler() {
        val generated = Foo.integrationFlow {
            sender = "Foo"
            https(
                url = "/exceptions",
                userRole = "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send",
                xsrfProtection = true,
                clientCertificates = true,
            ) {
                startMessage("CustomStart")
                groovyScript(
                    function = "convert1",
                    file = "action.groovy",
                )
                endMessage("CustomEnd")

                exceptionSubprocess {
                    startErrorEvent("ErrorStartHandling")
                    groovyScript(
                        function = "convert1",
                        file = "action.groovy",
                    )
                    errorEndEvent("ErrorEndHandling")
                }
            }
        }
        assertEquals(expected = exceptionHandling, actual = generated)
    }

    @Test
    fun withEmptyExceptionHandler() {
        val generated = Foo.integrationFlow {
            sender = "Foo"
            https(
                url = "/exceptions",
                userRole = "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send",
                xsrfProtection = true,
                clientCertificates = true,
            ) {
                startMessage("CustomStart")
                groovyScript(
                    function = "convert1",
                    file = "action.groovy",
                )
                endMessage("CustomEnd")

                exceptionSubprocess {
                    startErrorEvent("ErrorStartHandling")
                    errorEndEvent("ErrorEndHandling")
                }
            }
        }
        assertEquals(expected = emptyExceptionHandling, actual = generated)
    }
}
