// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry
import app.softwork.cikraft.Password

@ScriptEntry
fun foo(
    <!CIKRAFT_PASSWORD_IS_NOT_CHARARRAY!>@Password c: Int<!>,
    @Password d: CharArray,
) {}

/* GENERATED_FIR_TAGS: functionDeclaration */
