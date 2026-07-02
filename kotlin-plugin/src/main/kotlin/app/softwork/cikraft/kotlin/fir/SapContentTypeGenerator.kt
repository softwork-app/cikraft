package app.softwork.cikraft.kotlin.fir

import app.softwork.cikraft.kotlin.*
import app.softwork.cikraft.kotlin.fir.SapCIPredicateMatchingService.Companion.sapCIPredicateMatchingService
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.plugin.*
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.scopes.impl.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.*

@OptIn(DirectDeclarationsAccess::class)
public class SapContentTypeGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    internal companion object {
        internal val contentTypeFunctionName = Name.identifier("contentType")
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> = setOf(
        contentTypeFunctionName,
    )

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()

        return if (
            session.sapCIPredicateMatchingService.isAnnotatedWithContentType(owner) &&
            callableId.callableName == contentTypeFunctionName
        ) {
            require(owner is FirRegularClassSymbol)

            val hasContentTypeFunction = context.declaredScope?.hasContentTypeFunction() == true || lookupSuperTypes(
                owner,
                lookupInterfaces = true,
                deep = true,
                session,
            ).any {
                it.fullyExpandedType(session).toRegularClassSymbol(session)?.hasContentTypeFunction() ?: false
            }

            if (!hasContentTypeFunction) {
                listOf(generateContentTypeFunction(owner).symbol)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun FirClassDeclaredMemberScope.hasContentTypeFunction(): Boolean = getFunctions(
        contentTypeFunctionName,
    ).any {
        it.valueParameterSymbols.isEmpty()
    }

    private fun FirRegularClassSymbol.hasContentTypeFunction(): Boolean =
        declarationSymbols
            .filterIsInstance<FirNamedFunctionSymbol>()
            .any { it.isContentTypeFunction() }

    private fun FirNamedFunctionSymbol.isContentTypeFunction(): Boolean =
        name == contentTypeFunctionName && valueParameterSymbols.isEmpty()

    private fun generateContentTypeFunction(
        owner: FirRegularClassSymbol,
    ): FirNamedFunction = createMemberFunction(
        owner,
        SapCIFir,
        contentTypeFunctionName,
        session.builtinTypes.stringType.coneType,
    )
}
