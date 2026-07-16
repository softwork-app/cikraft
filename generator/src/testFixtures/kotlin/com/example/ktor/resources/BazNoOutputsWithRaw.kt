package com.example.ktor.resources

import com.example.FooInput
import com.example.dummyWithOutput
import com.example.twoPart1
import kotlin.Boolean
import kotlin.String

public suspend fun BazNoOutputsFunction(
  body: FooInput,
  b: String? = null,
  ignored: String? = null,
  injected: Boolean,
  ignored2: String? = null,
): BazNoOutputsResult {
  val resultTwoPart1 = twoPart1(body = body,b = b,ignored = ignored,injected = injected,)
  val resultDummyWithOutput = dummyWithOutput(body = resultTwoPart1.body,b = b,ignored2 = ignored2,)
  return BazNoOutputsResult(d = resultDummyWithOutput.d)
}

public data class BazNoOutputsResult(
  public val d: String,
)
