package app.softwork.cikraft.kotlin.runners

import app.softwork.cikraft.kotlin.services.ExtensionRegistrarConfigurator
import app.softwork.cikraft.kotlin.services.PluginAnnotationsProvider
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.builders.*
import org.jetbrains.kotlin.test.directives.*
import org.jetbrains.kotlin.test.runners.*
import org.jetbrains.kotlin.test.services.*

open class AbstractJvmDiagnosticTest : AbstractFirPhasedDiagnosticTest(FirParser.LightTree) {
    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        with(builder) {
            /*
             * Containers of different directives, which can be used in tests:
             * - ModuleStructureDirectives
             * - LanguageSettingsDirectives
             * - DiagnosticsDirectives
             * - FirDiagnosticsDirectives
             *
             * All of them are located in `org.jetbrains.kotlin.test.directives` package
             */
            defaultDirectives {
                +FirDiagnosticsDirectives.FIR_DUMP
                +JvmEnvironmentConfigurationDirectives.FULL_JDK
                +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
            }
            useConfigurators(
                ::PluginAnnotationsProvider,
                ::ExtensionRegistrarConfigurator,
            )
        }
    }

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}
