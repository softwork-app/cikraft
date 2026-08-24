package com.example.ktor.resources

import com.example.binaryRedirect
import com.sap.it.api.msglog.MessageLog
import kotlin.Nothing

context(messageLog: MessageLog)
public fun BazNoBodyFunction(): BazNoBodyResult {
  val resultBinaryRedirect = binaryRedirect()
  return BazNoBodyResult(nothing = resultBinaryRedirect.nothing)
}

public data class BazNoBodyResult(
  public val nothing: Nothing?,
)
