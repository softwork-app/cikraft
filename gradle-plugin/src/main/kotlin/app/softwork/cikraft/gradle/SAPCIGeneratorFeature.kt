package app.softwork.cikraft.gradle

import JvmDclTestSuite
import TestFixturesDefinition
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import javax.inject.Inject

@BindsProjectFeature(SAPCIGeneratorTestSuiteWrapper::class)
abstract class SAPCIGeneratorTestSuiteWrapper :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("cikraft", JvmDclTestSuiteApplyAction::class)
            .withBuildModelImplementationType(DefaultSAPCIGeneratorBuildModel::class.java)
        builder.bindProjectFeature("cikraft", JavaTestFixturesApplyAction::class)
            .withBuildModelImplementationType(DefaultSAPCIGeneratorBuildModel::class.java)
            .withUnsafeApplyAction()
    }

    abstract class JvmDclTestSuiteApplyAction :
        ProjectFeatureApplyAction<SAPCIGeneratorDefinition, SAPCIGeneratorBuildModel, JvmDclTestSuite> {
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

    abstract class JavaTestFixturesApplyAction :
        ProjectFeatureApplyAction<SAPCIGeneratorDefinition, SAPCIGeneratorBuildModel, TestFixturesDefinition> {
        @get:Inject
        abstract val sourceSets: SourceSetContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIGeneratorDefinition,
            buildModel: SAPCIGeneratorBuildModel,
            parentDefinition: TestFixturesDefinition,
        ) {
            buildModel as DefaultSAPCIGeneratorBuildModel
            buildModel.internalName = "testFixtures"
            buildModel.sourceDirectorySet = sourceSets.getByName("testFixtures").kotlin
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
