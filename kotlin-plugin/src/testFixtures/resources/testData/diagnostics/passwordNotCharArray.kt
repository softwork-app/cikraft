// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.ScriptEntry
import app.softwork.cikraft.Header
import app.softwork.cikraft.Password

@ScriptEntry
fun foo(
    <!CIKRAFT_PASSWORD_IS_NOT_CHARARRAY!>@Password c: Int<!>,
    @Password d: CharArray,
    <!CIKRAFT_ENTRYPOINT_HEADER_IS_NOT_NULLABLE_STRING!>@Header h: Int<!>,
    @Header correctHeader: String?,
    <!CIKRAFT_ENTRYPOINT_HEADER_IS_NOT_NULLABLE_STRING!>@Header wrongHeader: String<!>,
) {}

/* GENERATED_FIR_TAGS: functionDeclaration */
