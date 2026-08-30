package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.kotlin.dsl.newInstance
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import tel.schich.tinyjib.TinyJibExtension
import tel.schich.tinyjib.params.PlatformParameters
import javax.inject.Inject

// ideally, it lives in the Google jib repo, but it needs a full rework...
@BindsProjectFeature(JibFeature::class)
abstract class JibFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("tinyjib", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<JibDefinition, BuildModel.None, JvmApplicationProjectType> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val objectFactory: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: JibDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationProjectType,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            pluginManager.apply("tel.schich.tinyjib")

            val jib = project.extensions.getByName("tinyjib") as TinyJibExtension
            jib.apply {
                from {
                    image.set(definition.from.image)
                    platforms.set(
                        definition.from.platforms.elements.map {
                        it.map {
                            val (architecture, os) = it.name.split("/")
                            objectFactory.newInstance<PlatformParameters>().apply {
                                this.architecture.set(architecture)
                                this.os.set(os)
                            }
                        }
                    }
                    )
                }
                to.image.set(definition.image)
                container.mainClass.set(parentBuildModel.applications.getByName("main").mainClassName)
                container.ports.set(definition.ports)
                container.volumes.set(definition.volumes)
            }
        }
    }
}

interface JibDefinition : Definition<BuildModel.None> {
    @get:Nested
    val from: From
    val image: Property<String>

    @get:Nested
    val container: Container
}

interface From {
    val image: Property<String>

    @get:Nested
    val platforms: NamedDomainObjectContainer<Platform>
}

interface Platform : Named

interface Container {
  val ports: ListProperty<String>
  val volumes: ListProperty<String>
}
