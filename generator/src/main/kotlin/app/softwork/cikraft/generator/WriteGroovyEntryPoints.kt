package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*

public fun writeGroovyEntryPoints(
    scripts: List<Script>,
): String = buildString {
    appendLine("import com.sap.gateway.ip.core.customdev.util.Message")
    appendLine()

    for (entryPoint in scripts) {
        appendLine("Message ${entryPoint.name}(Message message) {")
        appendLine(
            "  return Entrypoints.${entryPoint.name}(message, messageLogFactory.getMessageLog(message))",
        )
        appendLine("}")
    }
}
