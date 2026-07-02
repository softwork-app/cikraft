package com.example.ktor.resources

import com.example.FooInput
import com.example.dummy
import com.example.dummyWithOutput
import com.example.twoPart1
import com.example.twoPart2
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.Map

public suspend fun BazTwoFunction(
  body: FooInput,
  b: String,
  ignored: String? = null,
  injected: Boolean,
  ignored2: String? = null,
): BazTwoResult {
  val resultTwoPart1 = twoPart1(body = body,b = b,ignored = ignored,injected = injected,)
  dummy(body = resultTwoPart1.body,b = b,ignored2 = ignored2,)
  val resultDummyWithOutput = dummyWithOutput(body = resultTwoPart1.body,b = b,ignored2 = ignored2,)
  val resultTwoPart2 = twoPart2(body = resultTwoPart1.body,b = b,ignored2 = ignored2,)
  return BazTwoResult(body = resultTwoPart2.body, fooHeader = resultTwoPart2.fooHeader, headers = resultTwoPart2.headers, d = resultDummyWithOutput.d)
}

public data class BazTwoResult(
  public val body: String,
  public val fooHeader: Int,
  public val d: String,
  public val headers: Map<String, String>,
)
