package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface APIProxiesDefinition : Definition<APIProxiesBuildModel> {
    val apiKeyStores: NamedDomainObjectContainer<ApiKeyStore>
    val apiCertificates: NamedDomainObjectContainer<ApiCertificate>
    val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProvider>
}

interface APIProxiesBuildModel : BuildModel {
    val apiKeyStores: NamedDomainObjectContainer<ApiKeyStore>
    val apiCertificates: NamedDomainObjectContainer<ApiCertificate>
    val apiRuntimeProviders: NamedDomainObjectContainer<ApiRuntimeProvider>
}
