package com.example.ktor.resources

import com.example.kotlinxIO
import com.sap.it.api.msglog.MessageLog
import kotlin.String
import kotlinx.io.Source

context(messageLog: MessageLog)
public fun BazKotlinxIOFunction(body: Source, b: String? = null): BazKotlinxIOResult {
  val resultKotlinxIO = kotlinxIO(body = body,b = b,)
  return BazKotlinxIOResult(body = resultKotlinxIO.body)
}

public data class BazKotlinxIOResult(
  public val body: Source,
)
