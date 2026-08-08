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

@BindsProjectFeature(SAPCIGeneratorTestSuiteWrapper::class)
abstract class SAPCIGeneratorTestSuiteWrapper :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("cikraft", JvmDclTestSuiteApplyAction::class)
    }

    abstract class ApplyAction<ParentBuildModel : BuildModel, ParentDefinition : Definition<ParentBuildModel>> :
        ProjectFeatureApplyAction<SAPCIGeneratorDefinition, SAPCIGeneratorBuildModel, ParentDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        fun apply(
            sourceDirectorySet: SourceDirectorySet,
            buildModel: DefaultSAPCIGeneratorBuildModel,
        ) {
            buildModel.sourceDirectorySet = sourceDirectorySet
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
            )
        }
    }
}

interface SAPCIGeneratorDefinition : Definition<SAPCIGeneratorBuildModel>

interface SAPCIGeneratorBuildModel : BuildModel, Named {
    val sourceDirectorySet: SourceDirectorySet
}

abstract class DefaultSAPCIGeneratorBuildModel : SAPCIGeneratorBuildModel {
    override fun getName(): String = internalName
    lateinit var internalName: String
    override lateinit var sourceDirectorySet: SourceDirectorySet
}
