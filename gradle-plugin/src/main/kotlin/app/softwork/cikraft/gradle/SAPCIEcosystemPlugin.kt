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
public abstract class SAPCIEcosystemPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement.components.withModule(R8_MODULE, R8VersionRule::class.java)

        settings.gradle.lifecycle.beforeProject {
            it.dependencies.attributesSchema {
                it.attribute(SAPCI.attribute)
            }
        }
        settings.dependencyResolutionManagement {
            it.versionCatalogs.register("cikraftLibs") {
                it.library("sapci-script-api", SAPCI_SCRIPT_API)
                it.library("sapci-generic-api", SAPCI_GENERIC_API)
                it.library("sapci-adapter-api", SAPCI_ADAPTER)
                it.library("sapci-groovy", SAPCI_GROOVY)

                it.library("runtime", "app.softwork.cikraft:runtime:$VERSION")
                it.library("core", "app.softwork.cikraft:core:$VERSION")
            }
        }
    }
}
