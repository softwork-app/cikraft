import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder

fun IntegrationFlowBuilder.integrationFlows() {
  IF_0100_Test_PW_SBX {
    https(
      url = "/auto-test",
      userRole = "ESBMessaging.send",
      xsrfProtection = true,
      clientCertificates = true,
      returnExceptionsToSender = true,
    ) {
      startMessage()
        typed(
          injected = {
            injectBoolean(
              result = { true },
            )
          }
        )
      endMessage()
    }
  }

  IF_0100_Test_PW_SBX_Exception {
    https(
      url = "/auto-test-exception",
      userRole = "ESBMessaging.send",
      xsrfProtection = true,
      clientCertificates = true,
      returnExceptionsToSender = true,
    ) {
      startMessage()
        typed(
          injected = {
            injectBoolean(
              result = { true },
            )
          }
        )
      endMessage()

      exceptionSubprocess {
        startErrorEvent("Error Start")
        contentModifier {
          setBody("adsfasdf")
          addHeader("CamelHttpResponseCode", "444")
        }
        endMessage("Error End")
      }
    }
  }
}
