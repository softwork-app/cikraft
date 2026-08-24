package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.fir.SapCIPredicateMatchingService.Companion.sapCIPredicateMatchingService
import app.softwork.cikraft.kotlin.messageLogClass
import app.softwork.cikraft.kotlin.throwsClass
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.checkers.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.*
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.*

internal data object ScriptEntryChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        val matcher = context.session.sapCIPredicateMatchingService
        if (matcher.isAnnotatedWithScriptEntry(declaration.symbol)) {
            declaration.mustBeStaticCallableFromGroovy()
            declaration.checkFault()
            declaration.checkParameters(matcher)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun FirNamedFunction.mustBeStaticCallableFromGroovy() {
        if (dispatchReceiverType != null) {
            reporter.reportOn(
                source,
                SapCIErrors.CIKRAFT_ENTRYPOINT_NOT_STATIC,
                symbol,
            )
        }

        if (receiverParameter != null) {
            reporter.reportOn(
                source,
                SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_RECEIVER,
                symbol,
            )
        }
        if (contextParameters.isNotEmpty()) {
            for (contextParameter in contextParameters) {
                if (!contextParameter.isMessageLog(checkNull = true)) {
                    reporter.reportOn(
                        source,
                        SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_UNSUPPORTED_CONTEXT_PARAMETER,
                        contextParameter.symbol,
                    )
                }
            }
        }

        if (typeParameters.isNotEmpty()) {
            reporter.reportOn(
                source,
                SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_TYPE_PARAMETERS,
                symbol,
            )
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun FirNamedFunction.checkFault() {
        val throwsAnnotation = getAnnotationByClassId(throwsClass, context.session)
        if (throwsAnnotation != null) {
            val classesArray =
                throwsAnnotation.findArgumentByName(
                    Name.identifier("exceptionClasses"),
                ) as FirVarargArgumentsExpression?
            if (classesArray != null && classesArray.arguments.size > 1) {
                reporter.reportOn(
                    throwsAnnotation.source,
                    SapCIErrors.CIKRAFT_ENTRYPOINT_HAS_MULTIPLE_THROWS,
                    symbol,
                )
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun FirNamedFunction.checkParameters(
        matcher: SapCIPredicateMatchingService,
    ) {
        for (parameter in valueParameters) {
            if (matcher.isAnnotatedWithPassword(parameter.symbol) && !parameter.isCharArray()) {
                reporter.reportOn(
                    parameter.source,
                    SapCIErrors.CIKRAFT_PASSWORD_IS_NOT_CHARARRAY,
                    parameter.symbol,
                )
            }

            if (matcher.isAnnotatedWithHeader(parameter.symbol) && !parameter.isNullableHeader()) {
                reporter.reportOn(
                    parameter.source,
                    SapCIErrors.CIKRAFT_ENTRYPOINT_HEADER_IS_NOT_NULLABLE_STRING,
                    parameter.symbol,
                )
            }

            if (parameter.isMessageLog(checkNull = false)) {
                reporter.reportOn(
                    parameter.source,
                    SapCIErrors.CIKRAFT_MESSAGELOG_MUST_BE_CONTEXT_PARAMETER,
                    parameter.symbol,
                )
            }
        }
    }

    context(context: CheckerContext)
    private fun FirValueParameter.isMessageLog(checkNull: Boolean): Boolean {
        val coneType = returnTypeRef.coneTypeOrNull
        val classId = coneType?.fullyExpandedClassId(context.session) ?: return false

        return if (checkNull) {
            classId == messageLogClass && !coneType.canBeNull(context.session)
        } else {
            classId == messageLogClass
        }
    }

    context(context: CheckerContext)
    private fun FirValueParameter.isCharArray(): Boolean {
        val classId = returnTypeRef.coneTypeOrNull?.fullyExpandedClassId(context.session) ?: return false
        return classId == CHAR_ARRAY
    }

    private val CHAR_ARRAY = StandardClassIds.byName("CharArray")

    context(context: CheckerContext)
    private fun FirVariable.isNullableHeader(): Boolean {
        val coneKotlinType = returnTypeRef.coneTypeOrNull?.fullyExpandedType() ?: return false
        return coneKotlinType.isNullableString
    }
}
