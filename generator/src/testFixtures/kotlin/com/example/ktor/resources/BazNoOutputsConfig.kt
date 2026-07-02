package com.example.ktor.resources

import app.softwork.cikraft.ktor.server.runtime.env
import kotlin.CharArray
import kotlin.Int

public data object BazNoOutputsConfig {
  public val cc: CharArray
    get() = env("BAZ_NOOUTPUTS_CC")!!.toCharArray()

  public val dd: CharArray
    get() = env("BAZ_NOOUTPUTS_DD")!!.toCharArray()

  public val ee: Int
    get() = env("BAZ_NOOUTPUTS_EE")?.toInt() ?: 0
}
