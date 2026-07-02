package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.fir.SapCIPredicateMatchingService.Companion.sapCIPredicateMatchingService
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.utils.isAbstract
import org.jetbrains.kotlin.fir.declarations.utils.visibility

internal data object ContentTypeChecker : FirRegularClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val matcher = context.session.sapCIPredicateMatchingService
        if (matcher.isAnnotatedWithContentType(declaration.symbol)) {
            when (declaration.classKind) {
                ClassKind.CLASS -> {
                    if (declaration.isAbstract || declaration.constructors(context.session).none {
                            it.visibility.isPublicAPI && it.valueParameterSymbols.isEmpty()
                        }
                    ) {
                        reporter.reportOn(
                            declaration.source,
                            SapCIErrors.CIKRAFT_CONTENT_TYPE_HAS_NOT_NO_ARG_CONSTRUCTOR,
                            declaration.symbol,
                        )
                    }
                }

                ClassKind.OBJECT -> {}

                else -> reporter.reportOn(
                    declaration.source,
                    SapCIErrors.CIKRAFT_CONTENT_TYPE_NOT_OBJECT_OR_CLASS,
                    declaration.symbol,
                )
            }
            if (!declaration.status.visibility.isPublicAPI) {
                reporter.reportOn(
                    declaration.source,
                    SapCIErrors.CIKRAFT_CONTENT_TYPE_NOT_PUBLIC,
                    declaration.symbol,
                )
            }
        }
    }
}
