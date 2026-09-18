package com.example.core

import app.softwork.cikraft.Property
import app.softwork.cikraft.ScriptEntry
import javax.script.ScriptException

@ScriptEntry
@Throws(Fault::class)
fun handleFault(
    @Property("CamelExceptionCaught") exception: Exception,
): Nothing {
    if (exception is ScriptException) {
        val nestedScriptRuntimeException = exception.cause!!
        val realException = nestedScriptRuntimeException.cause!!
        if (realException is Fault) {
            throw realException
        } else {
            throw realException
        }
    } else {
        throw exception
    }
}
