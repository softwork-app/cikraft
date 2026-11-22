package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface ApiRuntimeProvider :
    Definition<ApiRuntimeProviderBuildModel>,
    Named {
    public val title: Property<String>
    public val credentialStoreName: Property<String>
    public val credentialName: Property<String>
}

public interface ApiRuntimeProviderBuildModel :
    BuildModel,
    Named {
    public val title: Property<String>
    public val credentialStoreName: Property<String>
    public val credentialName: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
internal abstract class DefaultApiRuntimeProviderBuildModel : ApiRuntimeProviderBuildModel {
    @get:JvmName("internalName")
    lateinit var name: String
    override fun getName(): String = name
}
