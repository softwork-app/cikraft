package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_CONTENT_TYPE_NOT_OBJECT_OR_CLASS
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_CONTENT_TYPE_NOT_PUBLIC
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_DYNAMIC_HEADER_IS_NOT_MAP_STRING
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_MULTIPLE_THROWS
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_RECEIVER
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_TYPE_PARAMETERS
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_ENTRYPOINT_NOT_STATIC
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_HEADER_IS_NOT_PRIMITIVE
import app.softwork.cikraft.kotlin.fir.SapCIErrors.CIKRAFT_PASSWORD_IS_NOT_CHARARRAY
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.*
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers.DECLARATION_NAME

internal data object SapCIErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("CIKraft") {
        it.put(
            CIKRAFT_CONTENT_TYPE_NOT_OBJECT_OR_CLASS,
            "{0} is not an object or a public class with a no arg constructor",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR,
            "{0} does not have a no arg constructor",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_CONTENT_TYPE_NOT_PUBLIC,
            "{0} is not public",
            DECLARATION_NAME,
        )

        it.put(
            CIKRAFT_ENTRYPOINT_NOT_STATIC,
            "{0} is not static callable from Groovy by SAP Cloud Integration Suite",
            DECLARATION_NAME,
        )

        it.put(
            CIKRAFT_ENTRYPOINT_HAS_RECEIVER,
            "{0} has a receiver that is not callable from Groovy by SAP Cloud Integration Suite.",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_ENTRYPOINT_HAS_TYPE_PARAMETERS,
            "{0} has a type parameter that is not callable from Groovy by SAP Cloud Integration Suite.",
            DECLARATION_NAME,
        )

        it.put(
            CIKRAFT_ENTRYPOINT_HAS_MULTIPLE_THROWS,
            "{0} is annotated with multiple exceptions that is not compatible with SAP Cloud Integration Suite.",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_PASSWORD_IS_NOT_CHARARRAY,
            "The type of {0} is not kotlin.CharArray",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_DYNAMIC_HEADER_IS_NOT_MAP_STRING,
            "The type of {0} is not kotlin.Map<String, String>",
            DECLARATION_NAME,
        )
        it.put(
            CIKRAFT_HEADER_IS_NOT_PRIMITIVE,
            "The type of {0} is not a primitive like kotlin.String or kotlin.Int",
            DECLARATION_NAME,
        )
    }
}
