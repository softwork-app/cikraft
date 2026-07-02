package com.example.ktor.resources

import com.example.kotlinxIO
import com.sap.it.api.msglog.MessageLog
import kotlin.String
import kotlinx.io.Source

public fun BazKotlinxIOFunction(
  body: Source,
  b: String,
  rawNullableMessageLog: MessageLog? = null,
): BazKotlinxIOResult {
  val resultKotlinxIO = kotlinxIO(body = body,b = b,rawNullableMessageLog = rawNullableMessageLog,)
  return BazKotlinxIOResult(body = resultKotlinxIO.body)
}

public data class BazKotlinxIOResult(
  public val body: Source,
)
