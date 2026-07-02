package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.fir.SapCIPredicateMatchingService.Companion.sapCIPredicateMatchingService
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.checkers.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.*
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.*

internal data object DynamicHeaderChecker : FirPropertyChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val matcher = context.session.sapCIPredicateMatchingService
        if (matcher.isAnnotatedWithDynamicHeader(declaration.symbol) && !declaration.isStringStringMap()) {
            reporter.reportOn(
                declaration.source,
                SapCIErrors.CIKRAFT_DYNAMIC_HEADER_IS_NOT_MAP_STRING,
                declaration.symbol,
            )
        }
    }
}

context(context: CheckerContext)
private fun FirProperty.isStringStringMap(): Boolean {
    val coneKotlinType = returnTypeRef.coneTypeOrNull?.fullyExpandedType() ?: return false
    val classId = coneKotlinType.classId ?: return false
    val isMap = classId == StandardClassIds.Map
    if (isMap) {
        val (key, value) = coneKotlinType.typeArguments
        return key.type?.isString == true && value.type?.isString == true
    } else {
        return false
    }
}

internal data object PrimitiveHeaderPropertyChecker : FirPropertyChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val matcher = context.session.sapCIPredicateMatchingService
        if (matcher.isAnnotatedWithHeader(declaration.symbol) && !declaration.isPrimitive()) {
            reporter.reportOn(
                declaration.source,
                SapCIErrors.CIKRAFT_HEADER_IS_NOT_PRIMITIVE,
                declaration.symbol,
            )
        }
    }
}

internal data object PrimitiveHeaderParameterChecker : FirValueParameterChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirValueParameter) {
        val matcher = context.session.sapCIPredicateMatchingService
        if (matcher.isAnnotatedWithHeader(declaration.symbol) && !declaration.isPrimitive()) {
            reporter.reportOn(
                declaration.source,
                SapCIErrors.CIKRAFT_HEADER_IS_NOT_PRIMITIVE,
                declaration.symbol,
            )
        }
    }
}

context(context: CheckerContext)
private fun FirVariable.isPrimitive(): Boolean {
    val coneKotlinType = returnTypeRef.coneTypeOrNull?.fullyExpandedType() ?: return false
    return coneKotlinType.isPrimitiveOrNullablePrimitive || coneKotlinType.isString || coneKotlinType.isNullableString
}
