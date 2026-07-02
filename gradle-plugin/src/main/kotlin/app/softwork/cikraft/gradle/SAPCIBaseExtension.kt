package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface SAPCIInfrastructureDefinition : Definition<SAPCIInfrastructureBuildModel> {
    @get:Nested
    val apiStages: NamedDomainObjectContainer<ApiStage>

    @get:Nested
    val transportStages: NamedDomainObjectContainer<TransportStage>

    val suffix: Property<String>

    val httpNamespace: Property<String>
}

interface SAPCIInfrastructureBuildModel : BuildModel {
    val apiStages: NamedDomainObjectContainer<ApiStage>
    val transportStages: NamedDomainObjectContainer<TransportStage>
    val suffix: Provider<String>
    val httpSuffix: Provider<String>
}

interface DefaultSAPCIInfrastructureBuildModel : SAPCIInfrastructureBuildModel {
    override val suffix: Property<String>
    override val httpSuffix: Property<String>
}
