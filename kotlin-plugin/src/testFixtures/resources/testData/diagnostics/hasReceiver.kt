// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry

<!CIKRAFT_ENTRYPOINT_HAS_RECEIVER!>@ScriptEntry
fun String.foo() {}<!>

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration */
