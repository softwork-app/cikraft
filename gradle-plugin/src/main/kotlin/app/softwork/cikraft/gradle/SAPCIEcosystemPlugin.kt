package app.softwork.cikraft.gradle

import io.github.hfhbd.r8.R8VersionRule
import io.github.hfhbd.r8.R8_MODULE
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.features.annotations.RegistersProjectFeatures

@RegistersProjectFeatures(
    InfrastructureFeature::class,
    InfrastructureIntegrationFlowsFeature::class,
    InfrastructureIntegrationFlowsR8Feature::class,
    OpenApiFeature::class,

    APIProxyFeature::class,

    KotlinJvmIFlowFeature::class,

    SAPCIGeneratorFeature::class,
    GenerateFunctionsFeature::class,
    GenerateClientSetupFeature::class,
    GenerateKtorResourcesFeature::class,
    GenerateKtorServerFeature::class,
    GeneratePropertiesFeature::class,

    // Just because there is no test suite feature
    JvmTestSuiteFeature::class,
    KotlinTestJvmTestSuiteFeature::class,

    // Workaround until https://github.com/softwork-app/cikraft/issues/17
    ApiFeature::class,
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
            versionCatalogs.register("ciKraftLibs") {
                library("scriptApi", SAPCI_SCRIPT_API)
                library("genericApi", SAPCI_GENERIC_API)
                library("adapterApi", SAPCI_ADAPTER)
                library("groovy", SAPCI_GROOVY)

                library("ktorServerRuntime", "app.softwork.cikraft:ktor-server-runtime:$VERSION")
                library("runtime", "app.softwork.cikraft:runtime:$VERSION")
                library("core", "app.softwork.cikraft:core:$VERSION")
            }
        }
    }
}
