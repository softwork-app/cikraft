package app.softwork.cikraft.kotlin.fir

import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.psi.KtElement

internal data object SapCIErrors : KtDiagnosticsContainer() {
    val CIKRAFT_ENTRYPOINT_NOT_STATIC by error1<KtElement, FirNamedFunctionSymbol>()
    val CIKRAFT_ENTRYPOINT_HAS_RECEIVER by error1<KtElement, FirNamedFunctionSymbol>()
    val CIKRAFT_ENTRYPOINT_HAS_TYPE_PARAMETERS by error1<KtElement, FirNamedFunctionSymbol>()
    val CIKRAFT_ENTRYPOINT_HAS_MULTIPLE_THROWS by error1<KtElement, FirNamedFunctionSymbol>()
    val CIKRAFT_CONTENT_TYPE_NOT_OBJECT_OR_CLASS by error1<KtElement, FirRegularClassSymbol>()
    val CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR by error1<KtElement, FirRegularClassSymbol>()
    val CIKRAFT_CONTENT_TYPE_NOT_PUBLIC by error1<KtElement, FirRegularClassSymbol>()
    val CIKRAFT_PASSWORD_IS_NOT_CHARARRAY by error1<KtElement, FirValueParameterSymbol>()
    val CIKRAFT_ENTRYPOINT_HEADER_IS_NOT_NULLABLE_STRING by error1<KtElement, FirValueParameterSymbol>()
    val CIKRAFT_DYNAMIC_HEADER_IS_NOT_MAP_STRING by error1<KtElement, FirPropertySymbol>()
    val CIKRAFT_HEADER_IS_NOT_PRIMITIVE by error1<KtElement, FirVariableSymbol<*>>()

    override fun getRendererFactory() = SapCIErrorMessages
}
