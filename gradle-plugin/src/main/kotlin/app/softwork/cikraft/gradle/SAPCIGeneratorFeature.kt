package app.softwork.cikraft.gradle

import JvmDclTestSuite
import JvmDclTestSuiteBuildModel
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationBuildModel
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(SAPCIGeneratorFeature::class)
abstract class SAPCIGeneratorFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("ciKraft", JvmDclTestSuiteApplyAction::class)
            .withBuildModelImplementationType(DefaultSAPCIGeneratorBuildModel::class.java).withUnsafeDefinition()
            .withUnsafeApplyAction()

        builder.bindProjectFeature("ciKraft", JvmApplicationApplyAction::class)
            .withBuildModelImplementationType(DefaultSAPCIGeneratorBuildModel::class.java).withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction<ParentBuildModel : BuildModel, ParentDefinition : Definition<ParentBuildModel>> :
        ProjectFeatureApplyAction<SAPCIGeneratorDefinition, SAPCIGeneratorBuildModel, ParentDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        fun apply(
            sourceDirectorySet: SourceDirectorySet,
            buildModel: DefaultSAPCIGeneratorBuildModel,
            infrastructure: DependencyCollector,
        ) {
            buildModel.sourceDirectorySet = sourceDirectorySet
            buildModel.sapCICreatedFlows.fileProvider(
                configurations.resolvable("cikraftCreatedFlow" + buildModel.name) {
                    fromDependencyCollector(infrastructure)
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                        attribute(SAPCI.attribute, named(SAPCI.API))
                    }
                }.flatMap { it.elements }.map { it.single().asFile },
            )
        }
    }

    abstract class JvmDclTestSuiteApplyAction : ApplyAction<JvmDclTestSuiteBuildModel, JvmDclTestSuite>() {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIGeneratorDefinition,
            buildModel: SAPCIGeneratorBuildModel,
            parentDefinition: JvmDclTestSuite,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            buildModel as DefaultSAPCIGeneratorBuildModel
            buildModel.internalName = parentBuildModel.testSuite.name

            apply(
                sourceDirectorySet = parentBuildModel.testSuite.sources.kotlin,
                buildModel = buildModel,
                infrastructure = definition.dependencies.infrastructure,
            )
        }
    }

    abstract class JvmApplicationApplyAction : ApplyAction<JvmApplicationBuildModel, JvmApplicationProjectType>() {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIGeneratorDefinition,
            buildModel: SAPCIGeneratorBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            buildModel as DefaultSAPCIGeneratorBuildModel
            buildModel.internalName = ""
            apply(
                sourceDirectorySet = parentBuildModel.compilationUnits.getByName("main").sources,
                buildModel = buildModel,
                infrastructure = definition.dependencies.infrastructure,
            )
        }
    }
}

interface SAPCIGeneratorDefinition : Definition<SAPCIGeneratorBuildModel> {
    @get:Nested
    val dependencies: IFlowDependencies
}

interface SAPCIGeneratorBuildModel :
    HasSourceDirectorySet,
    BuildModel,
    Named {
    val sapCICreatedFlows: Provider<Directory>
}

abstract class DefaultSAPCIGeneratorBuildModel : SAPCIGeneratorBuildModel {
    override fun getName(): String = internalName
    lateinit var internalName: String
    override lateinit var sourceDirectorySet: SourceDirectorySet
    abstract override val sapCICreatedFlows: DirectoryProperty
}
