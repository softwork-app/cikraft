package com.example.ktor.resources

import app.softwork.cikraft.DataStoreMessages
import com.example.fooDataStore
import javax.net.ssl.KeyManager
import javax.sql.DataSource
import kotlin.Boolean
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.collections.Map

public fun BazDataStoreFunction(
  entries: DataStoreMessages<String>,
  c: CharArray,
  d: CharArray,
  e: Int? = null,
  km: KeyManager,
  ds: DataSource? = null,
  injected: Boolean,
  ignored: String? = null,
): BazDataStoreResult {
  val resultFooDataStore = fooDataStore(entries = entries,c = c,d = d,e = e,km = km,ds = ds,injected = injected,ignored = ignored,)
  return BazDataStoreResult(body = resultFooDataStore.body, fooHeader = resultFooDataStore.fooHeader, optionalHeader = resultFooDataStore.optionalHeader, headers = resultFooDataStore.headers)
}

public data class BazDataStoreResult(
  public val body: String,
  public val optionalHeader: String?,
  public val fooHeader: Int,
  public val headers: Map<String, String>,
)
