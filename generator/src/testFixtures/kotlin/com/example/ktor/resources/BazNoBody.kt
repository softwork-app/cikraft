package com.example.ktor.resources

object BazNoBody

fun BazNoBodyFunction() = NoBodyResult

data object NoBodyResult {
    val body = null
}
