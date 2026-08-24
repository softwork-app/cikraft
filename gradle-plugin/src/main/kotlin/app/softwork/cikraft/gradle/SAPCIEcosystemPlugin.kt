package app.softwork.cikraft.gradle

import TestFixturesFeature
import io.github.hfhbd.r8.R8VersionRule
import io.github.hfhbd.r8.R8_MODULE
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.features.annotations.RegistersProjectFeatures

@RegistersProjectFeatures(
    CIKraftBaseFeature::class,

    InfrastructureFeature::class,
    InfrastructureIntegrationFlowsFeature::class,
    InfrastructureIntegrationFlowsR8Feature::class,
    OpenApiFeature::class,

    APIProxyFeature::class,

    SAPCIGeneratorTestSuiteWrapper::class,
    GenerateFunctionsFeature::class,
    GenerateClientSetupFeature::class,
    GenerateKtorResourcesFeature::class,
    GenerateKtorServerFeature::class,
    GeneratePropertiesFeature::class,

    // Just because there is no test suite feature
    JvmTestSuiteFeature::class,
    KotlinTestJvmTestSuiteFeature::class,
    TestFixturesFeature::class,
)
abstract class SAPCIEcosystemPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement.components.withModule(R8_MODULE, R8VersionRule::class.java)

        settings.gradle.lifecycle.beforeProject {
            dependencies.attributesSchema {
                attribute(SAPCI.attribute)
                attribute(SAPCIStage.attribute)
            }
        }
        settings.dependencyResolutionManagement {
            versionCatalogs.register("cikraftLibs") {
                library("script-api", SAPCI_SCRIPT_API)
                library("generic-api", SAPCI_GENERIC_API)
                library("adapter-api", SAPCI_ADAPTER)
                library("groovy", SAPCI_GROOVY)

                library("ktor-server-runtime", "app.softwork.cikraft:ktor-server-runtime:$VERSION")
                library("runtime", "app.softwork.cikraft:runtime:$VERSION")
                library("core", "app.softwork.cikraft:core:$VERSION")
            }
        }
    }
}
