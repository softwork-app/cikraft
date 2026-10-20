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

interface APIProxiesDefinition : Definition<APIProxiesBuildModel> {
    val apiKeyStores: NamedDomainObjectContainer<ApiKeyStore>
    val apiCertificates: NamedDomainObjectContainer<ApiCertificate>
    val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProvider>
    val apiProxies: NamedDomainObjectContainer<ApiProxy>

    @get:Nested val dependencies: JvmComponentDependencies
}

interface APIProxiesBuildModel : BuildModel {
    val apiKeyStores: NamedDomainObjectContainer<ApiKeyStoreBuildModel>
    val apiCertificates: NamedDomainObjectContainer<ApiCertificateBuildModel>
    val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProviderBuildModel>
    val apiProxies: NamedDomainObjectContainer<ApiProxyBuildModel>
    val httpSuffix: Provider<String>
    val stages: NamedDomainObjectContainer<Stage>

    val classes: FileCollection
    val runtimeClasspath: FileCollection
}

abstract class DefaultAPIProxiesBuildModel : APIProxiesBuildModel {
    abstract override val httpSuffix: Property<String>
    abstract override val classes: ConfigurableFileCollection
    abstract override val runtimeClasspath: ConfigurableFileCollection
}

interface ApiProxy : Definition<ApiProxyBuildModel>, Named {
    val description: Property<String>
    /**
     * The name, not the stage specific id
     */
    val virtualHostName: Property<String>
}

interface ApiProxyBuildModel : BuildModel, Named {
    val description: Property<String>
    /**
     * The name, not the stage specific id
     */
    val virtualHostName: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
abstract class DefaultApiProxyBuildModel : ApiProxyBuildModel {
    lateinit var name: String
    override fun getName(): String = name
}
