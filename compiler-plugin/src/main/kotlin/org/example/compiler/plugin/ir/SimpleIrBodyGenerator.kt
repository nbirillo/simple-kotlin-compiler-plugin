package org.example.compiler.plugin.ir

import org.example.compiler.plugin.fir.SimpleClassGenerator
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl

class SimpleIrBodyGenerator(pluginContext: IrPluginContext) : AbstractTransformerForGenerator(pluginContext) {
    // We should use the key from SimpleClassGenerator to identify the classes and functions for generation
    override fun interestedIn(key: GeneratedDeclarationKey) = key == SimpleClassGenerator.Key

    override fun generateBodyForFunction(function: IrSimpleFunction, key: GeneratedDeclarationKey): IrBody {
        // We need to generate body only for this function: fun foo(): String = "Hello world"
        require(function.name == SimpleClassGenerator.FOO_ID.callableName)
        // Generate string constant
        val const = IrConstImpl(-1, -1, irBuiltIns.stringType, IrConstKind.String, value = "Hello world")
        // We need to return the string const from the function
        val returnStatement = IrReturnImpl(-1, -1, irBuiltIns.nothingType, function.symbol, const)
        return irFactory.createBlockBody(-1, -1, listOf(returnStatement))
    }

    override fun generateBodyForConstructor(constructor: IrConstructor, key: GeneratedDeclarationKey): IrBody? {
        // We have only a default constructor in the generated class
        return generateBodyForDefaultConstructor(constructor)
    }
}