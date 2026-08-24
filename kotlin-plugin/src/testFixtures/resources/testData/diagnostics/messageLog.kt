// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry

@ScriptEntry
context(log: com.sap.it.api.msglog.MessageLog)
fun ok() {
}

<!CIKRAFT_ENTRYPOINT_HAS_UNSUPPORTED_CONTEXT_PARAMETER!>@ScriptEntry
context(log: com.sap.it.api.msglog.MessageLog?)
fun wrongNull() {
}<!>

@ScriptEntry
fun wrong(
    <!CIKRAFT_MESSAGELOG_MUST_BE_CONTEXT_PARAMETER!>log: com.sap.it.api.msglog.MessageLog<!>,
    <!CIKRAFT_MESSAGELOG_MUST_BE_CONTEXT_PARAMETER!>log2: com.sap.it.api.msglog.MessageLog?<!>,
) {}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration */
