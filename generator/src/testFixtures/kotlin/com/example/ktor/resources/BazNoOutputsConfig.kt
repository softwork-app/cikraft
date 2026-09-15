package com.example.ktor.resources

import java.lang.System.getenv
import kotlin.CharArray
import kotlin.Int

public data object BazNoOutputsConfig {
  public val cc: CharArray
    get() = getenv("BAZ_NOOUTPUTS_CC")!!.toCharArray()

  public val dd: CharArray
    get() = getenv("BAZ_NOOUTPUTS_DD")!!.toCharArray()

  public val ee: Int
    get() = getenv("BAZ_NOOUTPUTS_EE")?.toInt() ?: 0
}
