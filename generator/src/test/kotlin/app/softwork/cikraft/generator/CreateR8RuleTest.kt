package app.softwork.cikraft.generator

import com.example.*
import fooScript
import fooScriptNoError
import fooSuspendScript
import noOutputsScript
import rawScript
import rawSuspendScript
import kotlin.test.*

class CreateR8RuleTest {
    @Test
    fun all() {
        assertEquals(
            """-dontobfuscate
-allowaccessmodification
-keepattributes SourceFile, LineNumberTable
-keep public class Entrypoints {
public static com.sap.gateway.ip.core.customdev.util.Message foo(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message fooSuspend(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message serialized(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message typed(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message noError(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message raw(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message rawSuspend(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
public static com.sap.gateway.ip.core.customdev.util.Message noOutputs(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);
}
""",
            createR8Rule(
                scripts = listOf(
                    fooScript,
                    fooSuspendScript,
                    serializedScript,
                    typedScript,
                    fooScriptNoError,
                    rawScript,
                    rawSuspendScript,
                    noOutputsScript,
                ),
            ),
        )
    }

    @Test
    fun empty() {
        assertEquals(
            """-dontobfuscate
-allowaccessmodification
-keepattributes SourceFile, LineNumberTable
""",
            createR8Rule(
                scripts = listOf(),
            ),
        )
    }
}
