@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import com.example.FooInput
import com.example.foo
import javax.net.ssl.KeyManager
import javax.sql.DataSource
import kotlin.Boolean
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.collections.Map

public fun BazAFunction(
  body: FooInput,
  b: String? = null,
  c: CharArray,
  d: CharArray,
  e: Int? = null,
  km: KeyManager,
  ds: DataSource? = null,
  injected: Boolean,
  ignored: String? = null,
): BazAResult {
  val resultFoo = foo(body = body,b = b,c = c,d = d,e = e,km = km,ds = ds,injected = injected,ignored = ignored,)
  return BazAResult(body = resultFoo.body, fooHeader = resultFoo.fooHeader, optionalHeader = resultFoo.optionalHeader, headers = resultFoo.headers)
}

public data class BazAResult(
  public val body: String,
  public val optionalHeader: String?,
  public val fooHeader: Int,
  public val headers: Map<String, String>,
)
