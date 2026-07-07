package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit

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

    val compilationUnits: NamedDomainObjectContainer<JvmCompilationUnit>
}

internal abstract class DefaultSAPCIInfrastructureBuildModel : SAPCIInfrastructureBuildModel {
    abstract override val suffix: Property<String>
    abstract override val httpSuffix: Property<String>
    override lateinit var compilationUnits: NamedDomainObjectContainer<JvmCompilationUnit>
}
