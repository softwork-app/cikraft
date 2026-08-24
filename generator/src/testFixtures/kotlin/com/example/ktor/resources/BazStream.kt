package com.example.ktor.resources

import com.example.javaStreams
import com.sap.it.api.msglog.MessageLog
import java.io.InputStream
import kotlin.String

context(messageLog: MessageLog)
public fun BazStreamFunction(body: InputStream, b: String? = null): BazStreamResult {
  val resultJavaStreams = javaStreams(body = body,b = b,)
  return BazStreamResult(body = resultJavaStreams.body)
}

public data class BazStreamResult(
  public val body: InputStream,
)
