// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry

class M {
    <!CIKRAFT_ENTRYPOINT_NOT_STATIC!>@ScriptEntry
    fun foo(
    ) {}<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration */
