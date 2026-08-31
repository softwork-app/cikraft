package app.softwork.cikraft.gradle

import io.github.hfhbd.jib.JibFeaturesPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.features.annotations.RegistersProjectFeatures

@RegistersProjectFeatures(
    DockerEnvironmentFeature::class,
)
abstract class DockerComposeFeaturePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.pluginManager.apply(JibFeaturesPlugin::class.java)
    }
}
