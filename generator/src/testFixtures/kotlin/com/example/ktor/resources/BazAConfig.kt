package com.example.ktor.resources

import java.lang.System.getenv
import kotlin.CharArray
import kotlin.Int

public data object BazAConfig {
  public val c: CharArray
    get() = getenv("BAZ_A_C")!!.toCharArray()

  public val d: CharArray
    get() = getenv("BAZ_A_D")!!.toCharArray()

  public val e: Int
    get() = getenv("BAZ_A_E")?.toInt() ?: 0
}
