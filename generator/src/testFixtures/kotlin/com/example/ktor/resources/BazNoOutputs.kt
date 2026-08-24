package com.example.ktor.resources

import com.example.noOutputs
import com.sap.it.api.msglog.MessageLog
import kotlin.CharArray
import kotlin.Int
import kotlin.String

context(messageLog: MessageLog)
public fun BazNoOutputsFunction(
  bb: String? = null,
  cc: CharArray,
  dd: CharArray,
  ee: Int? = null,
  ignored: String? = null,
) {
  noOutputs(bb = bb,cc = cc,dd = dd,ee = ee,ignored = ignored,)
}
