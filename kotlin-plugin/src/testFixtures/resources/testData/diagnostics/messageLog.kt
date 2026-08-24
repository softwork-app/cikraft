// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry

@ScriptEntry
context(log: com.sap.it.api.msglog.MessageLog)
fun ok() {
}

@ScriptEntry
fun wrong(
    <!CIKRAFT_MESSAGELOG_MUST_BE_CONTEXT_PARAMETER!>log: com.sap.it.api.msglog.MessageLog<!>
) {}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration */
