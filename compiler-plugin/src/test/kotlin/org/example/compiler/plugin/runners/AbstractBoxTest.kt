package org.example.compiler.plugin.runners

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.handlers.IrTextDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.IrTreeVerifierHandler
import org.jetbrains.kotlin.test.backend.ir.JvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.fir2IrStep
import org.jetbrains.kotlin.test.builders.irHandlersStep
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.test.runners.RunnerWithTargetBackendForTestGeneratorMarker

open class AbstractBoxTest : BaseTestRunner(), RunnerWithTargetBackendForTestGeneratorMarker {
    override val targetBackend: TargetBackend
        get() = TargetBackend.JVM_IR

    override fun TestConfigurationBuilder.configuration() {
        globalDefaults {
            targetBackend = TargetBackend.JVM_IR
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            dependencyKind = DependencyKind.Binary
        }

        defaultDirectives {
            +CodegenTestDirectives.DUMP_IR
            +ConfigurationDirectives.WITH_STDLIB
            JvmEnvironmentConfigurationDirectives.JVM_TARGET with JvmTarget.JVM_11
            +CodegenTestDirectives.IGNORE_DEXING
            +JvmEnvironmentConfigurationDirectives.FULL_JDK
        }

        commonFirWithPluginFrontendConfiguration()
        fir2IrStep()
        irHandlersStep {
            useHandlers(
                ::IrTextDumpHandler,
                ::IrTreeVerifierHandler,
            )
        }
        facadeStep(::JvmIrBackendFacade)

        useAfterAnalysisCheckers(::BlackBoxCodegenSuppressor)
        enableMetaInfoHandler()
    }
}