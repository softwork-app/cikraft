package com.example.ktor.resources

import com.sap.it.api.msglog.MessageLog
import nullableReturn

context(messageLog: MessageLog)
public fun BazDataStoreFunction() {
  val resultNullableReturn = nullableReturn() ?: return
}
