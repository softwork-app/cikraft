import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(ApiFeature::class)
abstract class ApiFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("api", ApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<ApiDefinition, BuildModel.None, JvmApplicationProjectType> {
        @get:Inject
        abstract val configurations: ConfigurationContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: ApiDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationProjectType,
        ) {
            configurations.getByName("api").fromDependencyCollector(definition.dependencies.api)
        }
    }
}

interface ApiDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: ApiDependencies
}

interface ApiDependencies : Dependencies {
    val api: DependencyCollector
}
