package app.softwork.cikraft.kotlin

import app.softwork.cikraft.kotlin.fir.*
import app.softwork.cikraft.kotlin.ir.SapCIIRExtensionRegistrar
import io.github.hfhbd.serviceloader.ServiceLoader
import org.jetbrains.kotlin.backend.common.extensions.*
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.fir.extensions.*

@ServiceLoader(CompilerPluginRegistrar::class)
public class SapCIPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true
    override val pluginId: String = PLUGIN_ID

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        sapCI()
    }

    internal companion object {
        const val PLUGIN_ID = "app.softwork.cikraft.kotlin"

        fun ExtensionStorage.sapCI() {
            FirExtensionRegistrarAdapter.registerExtension(SapCIFirExtensionRegistrar)
            IrGenerationExtension.registerExtension(SapCIIRExtensionRegistrar)
        }
    }
}
