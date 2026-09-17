package app.softwork.cikraft.kotlin.ir

import app.softwork.cikraft.kotlin.SapCIFir
import app.softwork.cikraft.kotlin.contentTypeFq
import app.softwork.cikraft.kotlin.fir.SapContentTypeGenerator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getConstArgument
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.Name

internal class SapCIContentTypeVisitor(private val pluginContext: IrPluginContext) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        declaration.acceptChildrenVoid(this)

        val origin = declaration.origin
        if (declaration.name == SapContentTypeGenerator.contentTypeFunctionName &&
            (origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey == SapCIFir)
        ) {
            val contentTypeConstructor = declaration.parentAsClass.getAnnotation(contentTypeFq)!!
            val contentTypeValue = contentTypeConstructor.getConstArgument<String>("value")!!

            val contentTypeParameters = contentTypeConstructor.argumentMapping[
                Name.identifier(
                    "parameters",
                ),
            ] as IrVararg?
            val computedValue = if (contentTypeParameters == null) {
                contentTypeValue
            } else {
                contentTypeParameters.elements.joinToString(
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
