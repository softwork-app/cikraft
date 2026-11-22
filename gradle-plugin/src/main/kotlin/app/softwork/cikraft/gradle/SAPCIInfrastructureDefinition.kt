package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface SAPCIInfrastructureDefinition : Definition<SAPCIInfrastructureBuildModel> {
    public val apiStages: NamedDomainObjectContainer<ApiStage>

    public val transportStages: NamedDomainObjectContainer<TransportStage>

    public val suffix: Property<String>

    public val httpNamespace: Property<String>
}

public interface SAPCIInfrastructureBuildModel : BuildModel {
    public val apiStages: NamedDomainObjectContainer<ApiStage>
    public val transportStages: NamedDomainObjectContainer<TransportStage>
    public val suffix: Provider<String>
    public val httpSuffix: Provider<String>
}

internal abstract class DefaultSAPCIInfrastructureBuildModel : SAPCIInfrastructureBuildModel {
    abstract override val suffix: Property<String>
    abstract override val httpSuffix: Property<String>
}
