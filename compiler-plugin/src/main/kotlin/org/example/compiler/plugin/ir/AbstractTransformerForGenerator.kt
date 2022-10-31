package org.example.compiler.plugin.ir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

abstract class AbstractTransformerForGenerator(context: IrPluginContext) : IrElementVisitorVoid {
    protected val irFactory = context.irFactory
    protected val irBuiltIns = context.irBuiltIns

    abstract fun interestedIn(key: GeneratedDeclarationKey): Boolean
    abstract fun generateBodyForFunction(function: IrSimpleFunction, key: GeneratedDeclarationKey): IrBody?
    abstract fun generateBodyForConstructor(constructor: IrConstructor, key: GeneratedDeclarationKey): IrBody?

    final override fun visitElement(element: IrElement) {
        when (element) {
            is IrDeclaration,
            is IrFile,
            is IrModuleFragment -> element.acceptChildrenVoid(this)
            else -> {}
        }
    }

    final override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        visitSimpleFunctionOrConstructor(declaration) { d, k -> generateBodyForFunction(d as IrSimpleFunction, k) }
    }

    final override fun visitConstructor(declaration: IrConstructor) {
        visitSimpleFunctionOrConstructor(declaration) { d, k -> generateBodyForConstructor(d as IrConstructor, k) }
    }

    // ------------------------ utilities ------------------------

    protected fun generateBodyForDefaultConstructor(declaration: IrConstructor): IrBody? {
        val type = declaration.returnType as? IrSimpleType ?: return null

        val delegatingAnyCall = IrDelegatingConstructorCallImpl(
            -1,
            -1,
            irBuiltIns.anyType,
            irBuiltIns.anyClass.owner.primaryConstructor?.symbol ?: return null,
            typeArgumentsCount = 0,
            valueArgumentsCount = 0
        )

        val initializerCall = IrInstanceInitializerCallImpl(
            -1,
            -1,
            (declaration.parent as? IrClass)?.symbol ?: return null,
            type
        )

        return irFactory.createBlockBody(-1, -1, listOf(delegatingAnyCall, initializerCall))
    }

    private fun visitSimpleFunctionOrConstructor(
        declaration: IrFunction,
        generator: (IrFunction, GeneratedDeclarationKey) -> IrBody?
    ) {
        val origin = declaration.origin
        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || !interestedIn(origin.pluginKey)) return
        require(declaration.body == null)
        declaration.body = generator(declaration, origin.pluginKey)
    }
}