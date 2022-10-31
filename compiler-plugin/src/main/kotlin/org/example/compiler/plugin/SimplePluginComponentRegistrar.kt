package org.example.compiler.plugin

import org.example.compiler.plugin.ir.SimpleIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

// The main plugin registrar
@OptIn(ExperimentalCompilerApi::class)
class SimplePluginComponentRegistrar: CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    // Register all plugin's extensions
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(SimplePluginRegistrar())
        IrGenerationExtension.registerExtension(SimpleIrGenerationExtension())
    }
}