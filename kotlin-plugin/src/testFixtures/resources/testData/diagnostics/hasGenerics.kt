// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry

<!CIKRAFT_ENTRYPOINT_HAS_TYPE_PARAMETERS!>@ScriptEntry
fun <T> foo() {}<!>

/* GENERATED_FIR_TAGS: functionDeclaration, nullableType, typeParameter */
