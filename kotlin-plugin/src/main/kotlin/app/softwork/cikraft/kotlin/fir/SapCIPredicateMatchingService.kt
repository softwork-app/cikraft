package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.bodyFq
import app.softwork.cikraft.kotlin.contentTypeFq
import app.softwork.cikraft.kotlin.dynamicHeadersFq
import app.softwork.cikraft.kotlin.headerFq
import app.softwork.cikraft.kotlin.passwordFq
import app.softwork.cikraft.kotlin.propertyFq
import app.softwork.cikraft.kotlin.scriptEntryFq
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.JvmStandardClassIds

internal class SapCIPredicateMatchingService(session: FirSession) : FirExtensionSessionComponent(session) {
    private val scriptEntryPredicate = LookupPredicate.create {
        annotated(scriptEntryFq)
    }
    private val contentTypePredicate = DeclarationPredicate.create {
        annotated(contentTypeFq)
    }
    private val overloadsPredicate = LookupPredicate.create {
        annotated(JvmStandardClassIds.JVM_OVERLOADS_FQ_NAME)
    }
    private val inputPredicate = LookupPredicate.create {
        annotated(bodyFq) or annotated(headerFq) or annotated(propertyFq) or annotated(passwordFq)
    }
    private val passwordPredicate = LookupPredicate.create {
        annotated(passwordFq)
    }
    private val dynamicHeaderPredicate = LookupPredicate.create {
        annotated(dynamicHeadersFq)
    }
    private val headerPredicate = LookupPredicate.create {
        annotated(headerFq)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(scriptEntryPredicate)
        register(contentTypePredicate)
        register(overloadsPredicate)
        register(inputPredicate)
        register(passwordPredicate)
        register(dynamicHeaderPredicate)
        register(headerPredicate)
    }

    fun isAnnotatedWithScriptEntry(symbol: FirNamedFunctionSymbol) =
        session.predicateBasedProvider.matches(scriptEntryPredicate, symbol)

    fun isAnnotatedWithContentType(symbol: FirClassSymbol<*>) =
        session.predicateBasedProvider.matches(contentTypePredicate, symbol)

    fun isAnnotatedWithOverloads(symbol: FirNamedFunctionSymbol) =
        session.predicateBasedProvider.matches(overloadsPredicate, symbol)

    fun isAnnotatedWithInput(symbol: FirValueParameterSymbol) =
        session.predicateBasedProvider.matches(inputPredicate, symbol)

    fun isAnnotatedWithPassword(symbol: FirValueParameterSymbol) =
        session.predicateBasedProvider.matches(passwordPredicate, symbol)

    fun isAnnotatedWithDynamicHeader(symbol: FirPropertySymbol) =
        session.predicateBasedProvider.matches(dynamicHeaderPredicate, symbol)

    fun isAnnotatedWithHeader(symbol: FirPropertySymbol) =
        session.predicateBasedProvider.matches(headerPredicate, symbol)

    fun isAnnotatedWithHeader(symbol: FirValueParameterSymbol) =
        session.predicateBasedProvider.matches(headerPredicate, symbol)

    companion object {
        val FirSession.sapCIPredicateMatchingService: SapCIPredicateMatchingService by FirSession.sessionComponentAccessor()
    }
}
