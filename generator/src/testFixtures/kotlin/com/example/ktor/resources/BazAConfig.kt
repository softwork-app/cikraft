package com.example.ktor.resources

import app.softwork.cikraft.ktor.server.runtime.env
import kotlin.CharArray
import kotlin.Int

public data object BazAConfig {
  public val c: CharArray
    get() = env("BAZ_A_C")!!.toCharArray()

  public val d: CharArray
    get() = env("BAZ_A_D")!!.toCharArray()

  public val e: Int
    get() = env("BAZ_A_E")?.toInt() ?: 0
}
