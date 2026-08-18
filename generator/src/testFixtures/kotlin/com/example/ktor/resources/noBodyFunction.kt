package com.example.ktor.resources

import com.example.binaryRedirect
import kotlin.Nothing

public fun BazNoBodyFunction(): BazNoBodyResult {
  val resultBinaryRedirect = binaryRedirect()
  return BazNoBodyResult(nothing = resultBinaryRedirect.nothing)
}

public data class BazNoBodyResult(
  public val nothing: Nothing?,
)
