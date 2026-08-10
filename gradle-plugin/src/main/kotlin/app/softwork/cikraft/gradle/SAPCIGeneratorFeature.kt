package app.softwork.cikraft.gradle

import JvmDclTestSuite
import JvmDclTestSuiteBuildModel
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.registration.ConfigurationRegistrar
import javax.inject.Inject

@BindsProjectFeature(SAPCIGeneratorTestSuiteWrapper::class)
abstract class SAPCIGeneratorTestSuiteWrapper :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("cikraft", JvmDclTestSuiteApplyAction::class)
    }

    abstract class JvmDclTestSuiteApplyAction : ProjectFeatureApplyAction<SAPCIGeneratorDefinition, SAPCIGeneratorBuildModel, JvmDclTestSuite> {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIGeneratorDefinition,
            buildModel: SAPCIGeneratorBuildModel,
            parentDefinition: JvmDclTestSuite,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            buildModel as DefaultSAPCIGeneratorBuildModel
            buildModel.internalName = parentBuildModel.testSuite.name
            buildModel.sourceDirectorySet = parentBuildModel.testSuite.sources.kotlin
        }
    }
}

interface SAPCIGeneratorDefinition : Definition<SAPCIGeneratorBuildModel>

interface SAPCIGeneratorBuildModel :
    BuildModel,
    Named {
    val sourceDirectorySet: SourceDirectorySet
}

abstract class DefaultSAPCIGeneratorBuildModel : SAPCIGeneratorBuildModel {
    override fun getName(): String = internalName
    lateinit var internalName: String
    override lateinit var sourceDirectorySet: SourceDirectorySet
}
