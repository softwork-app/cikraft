package com.example.ktor.resources

import com.example.noOutputs
import kotlin.CharArray
import kotlin.Int
import kotlin.String

public fun BazNoOutputsFunction(
  bb: String,
  cc: CharArray,
  dd: CharArray,
  ee: Int? = null,
  ignored: String? = null,
) {
  noOutputs(bb = bb,cc = cc,dd = dd,ee = ee,ignored = ignored,)
}
