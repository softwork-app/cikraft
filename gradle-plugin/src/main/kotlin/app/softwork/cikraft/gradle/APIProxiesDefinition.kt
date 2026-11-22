package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface APIProxiesDefinition : Definition<APIProxiesBuildModel> {
    public val apiKeyStores: NamedDomainObjectContainer<ApiKeyStore>
    public val apiCertificates: NamedDomainObjectContainer<ApiCertificate>
    public val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProvider>
    public val apiProxies: NamedDomainObjectContainer<ApiProxy>

    @get:Nested
    public val dependencies: JvmComponentDependencies
}

public interface APIProxiesBuildModel : BuildModel {
    public val apiKeyStores: NamedDomainObjectContainer<ApiKeyStoreBuildModel>
    public val apiCertificates: NamedDomainObjectContainer<ApiCertificateBuildModel>
    public val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProviderBuildModel>
    public val apiProxies: NamedDomainObjectContainer<ApiProxyBuildModel>
    public val httpSuffix: Provider<String>
    public val stages: NamedDomainObjectContainer<Stage>

    public val classes: FileCollection
    public val runtimeClasspath: FileCollection
}

internal abstract class DefaultAPIProxiesBuildModel : APIProxiesBuildModel {
    abstract override val httpSuffix: Property<String>
    abstract override val classes: ConfigurableFileCollection
    abstract override val runtimeClasspath: ConfigurableFileCollection
}

public interface ApiProxy :
    Definition<ApiProxyBuildModel>,
    Named {
    public val description: Property<String>

    /**
     * The name, not the stage specific id
     */
    public val virtualHostName: Property<String>
}

public interface ApiProxyBuildModel :
    BuildModel,
    Named {
    public val description: Property<String>

    /**
     * The name, not the stage specific id
     */
    public val virtualHostName: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
internal abstract class DefaultApiProxyBuildModel : ApiProxyBuildModel {
    @get:JvmName("internalName")
    lateinit var name: String
    override fun getName(): String = name
}
