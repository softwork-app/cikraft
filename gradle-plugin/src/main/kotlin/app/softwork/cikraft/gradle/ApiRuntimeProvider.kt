package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface ApiRuntimeProvider : Definition<ApiRuntimeProviderBuildModel>, Named {
    val title: Property<String>
    val credentialStoreName: Property<String>
    val credentialName: Property<String>
}

interface ApiRuntimeProviderBuildModel : BuildModel, Named {
    val title: Property<String>
    val credentialStoreName: Property<String>
    val credentialName: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
abstract class DefaultApiRuntimeProviderBuildModel : ApiRuntimeProviderBuildModel {
    lateinit var name: String
    override fun getName(): String = name
}
