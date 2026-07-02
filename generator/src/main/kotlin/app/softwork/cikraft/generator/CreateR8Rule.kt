package app.softwork.cikraft.generator

import app.softwork.cikraft.core.Script

public fun createR8Rule(
    scripts: List<Script>,
): String = buildString {
    appendLine("-dontobfuscate")
    appendLine("-allowaccessmodification")
    appendLine("-keepattributes SourceFile, LineNumberTable")

    if (scripts.isNotEmpty()) {
        appendLine("-keep public class Entrypoints {")
        for (script in scripts) {
            appendLine(
                "public static com.sap.gateway.ip.core.customdev.util.Message ${script.name}(com.sap.gateway.ip.core.customdev.util.Message, com.sap.it.api.msglog.MessageLog);",
            )
        }
        appendLine("}")
    }
}
