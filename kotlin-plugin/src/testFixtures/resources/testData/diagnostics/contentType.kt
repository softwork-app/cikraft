// RUN_PIPELINE_TILL: FRONTEND

import app.softwork.cikraft.ContentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.StringFormat

@ContentType("text/xml", parameters = ["foo=value", "bar=value"])
data object TextFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})

@ContentType("text/xml", parameters = ["foo=value", "bar=value"])
class TextSoapFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})

<!CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR!>@ContentType("application/json", parameters = ["foo=value", "bar=value"])
abstract class JsonFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})<!>

<!CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR!>@ContentType("text/xml", parameters = ["foo=value", "bar=value"])
class TextWrongSoapFactory(val s: String)  : StringFormat by Json(builderAction = {
    prettyPrint = true
})<!>

/* GENERATED_FIR_TAGS: functionDeclaration, nullableType, typeParameter */
