// RUN_PIPELINE_TILL: FRONTEND

import app.softwork.cikraft.ScriptEntry

public data class Fault(
    override val message: String,
) : Exception(message)

@ScriptEntry
<!CIKRAFT_ENTRYPOINT_HAS_MULTIPLE_THROWS!>@Throws(Fault::class, java.io.IOException::class)<!>
fun foo() { }

/* GENERATED_FIR_TAGS: classDeclaration, classReference, data, functionDeclaration, override, primaryConstructor,
propertyDeclaration */
