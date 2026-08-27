package app.softwork.cikraft.gradle

import JvmDclTestSuiteTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.named
import javax.inject.Inject

@BindsProjectFeature(DockerComposeFeature::class)
abstract class DockerComposeFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("dockerCompose", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
        builder.bindProjectFeature("dockerCompose", ApplyTestSuiteAction::class)
            .withUnsafeApplyAction()
    }

    abstract class ApplyTestSuiteAction :
        ProjectFeatureApplyAction<DockerComposeTestSuiteTargetDefinition, BuildModel.None, JvmDclTestSuiteTarget> {
        @get:Inject
        abstract val tasks: TaskContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DockerComposeTestSuiteTargetDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmDclTestSuiteTarget,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            // https://github.com/gradle/gradle/issues/37376 to get the task via BuildModel of DockerCompose feature
            val generateDockerComposeFile =
                tasks.named("generateDockerComposeFile", GenerateDockerComposeFileTask::class.java)

            parentBuildModel.target.testTask.configure {
                inputs.files(generateDockerComposeFile.map { it.output })
                environment(
                    "DOCKER_COMPOSE_FILE",
                    generateDockerComposeFile.map { it.output.get().asFile.path }.get(),
                )
            }
        }
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<DockerComposeDefinition, BuildModel.None, JibDefinition> {
        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val components: SoftwareComponentContainer

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DockerComposeDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JibDefinition,
        ) {
            val containerWorkerDeps = configurations.dependencyScope("containerWorkerDeps") {
                @Suppress("INVISIBLE_REFERENCE")
                dependencies.add(
                    dependencyFactory.create("app.softwork.cikraft:core:${app.softwork.cikraft.gradle.VERSION}"),
                )
            }

            val containerWorkerClasspath = configurations.resolvable("containerWorkerClasspath") {
                extendsFrom(containerWorkerDeps)
            }

            val propertiesConfiguration = configurations.resolvable("dockerComposeProperties") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.STAGE_PROPERTIES))
                    attributeProvider(SAPCIStage.attribute, definition.stage.map { named(it) })
                }
            }

            val generateDockerComposeFile =
                tasks.register("generateDockerComposeFile", GenerateDockerComposeFileTask::class.java) {
                    projectName.set(definition.projectName)
                    serviceName.set(definition.serviceName)
                    propertyFiles.from(propertiesConfiguration)
                    image.set(parentDefinition.image)
                    volumes.set(definition.volumes)
                    output.convention(layout.contextBuildDirectory.map { it.file("cikraft/compose.yaml") })
                    workerClasspath.from(containerWorkerClasspath)
                }

            val dockerCompileFiles = configurations.consumable("dockerCompileFiles") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.DOCKER_COMPOSE))
                }
                outgoing {
                    artifact(generateDockerComposeFile)
                }
            }

            val component = components.getByName("java") as AdhocComponentWithVariants
            component.addVariantsFromConfiguration(dockerCompileFiles) {}
        }
    }
}

interface DockerComposeDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: DockerComposeDependencies

    val projectName: Property<String>
    val serviceName: Property<String>
    val stage: Property<String>
    val volumes: ListProperty<String>
}

interface DockerComposeDependencies : Dependencies {
    val infrastructure: DependencyCollector
}

interface DockerComposeTestSuiteTargetDefinition : Definition<BuildModel.None>
