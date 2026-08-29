package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.features.annotations.RegistersProjectFeatures

@RegistersProjectFeatures(
    JibFeature::class,
    DockerEnvironmentFeature::class,
)
abstract class DockerComposeFeaturePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {}
}
