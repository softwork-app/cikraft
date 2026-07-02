package app.softwork.cikraft.kotlin

import app.softwork.cikraft.kotlin.fir.*
import app.softwork.cikraft.kotlin.fir.SapContentTypeGenerator.Companion.contentTypeFunctionName
import app.softwork.serviceloader.*
import org.jetbrains.kotlin.backend.common.extensions.*
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.*

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

internal data object SapCIIRExtensionRegistrar : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        moduleFragment.acceptVoid(SapCIContentTypeVisitor(pluginContext))
    }
}

internal class SapCIContentTypeVisitor(private val pluginContext: IrPluginContext) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        declaration.acceptChildrenVoid(this)

        val origin = declaration.origin
        if (declaration.name == contentTypeFunctionName &&
            (origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey == SapCIFir)
        ) {
            val contentTypeConstructor = declaration.parentAsClass.getAnnotation(contentTypeFq)!!
            val contentTypeValue = (
                contentTypeConstructor.getValueArgument(
                    Name.identifier("value"),
                )!! as IrConst
                ).value as String

            val contentTypeParameters = contentTypeConstructor.getValueArgument(Name.identifier("parameters"))
            val computedValue = if (contentTypeParameters == null) {
                contentTypeValue
            } else {
                (contentTypeParameters as IrVararg).elements.joinToString(
                    separator = "; ",
                    prefix = "$contentTypeValue; ",
                ) { (it as IrConst).value as String }
            }

            declaration.body = declaration.factory.createExpressionBody(
                IrConstImpl.string(
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    pluginContext.irBuiltIns.stringType,
                    computedValue,
                ),
            )
        }
    }
}
