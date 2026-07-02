package app.softwork.cikraft.kotlin.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal data object SapCIFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::SapCIPredicateMatchingService
        +::SapCICheckerExtension
        +::SapContentTypeGenerator
    }
}
