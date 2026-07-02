package app.softwork.cikraft.kotlin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class SapCICheckerExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val simpleFunctionCheckers = setOf(ScriptEntryChecker)
        override val regularClassCheckers = setOf(ContentTypeChecker)
        override val propertyCheckers = setOf(DynamicHeaderChecker, PrimitiveHeaderPropertyChecker)
        override val valueParameterCheckers = setOf(PrimitiveHeaderParameterChecker)
    }
}
