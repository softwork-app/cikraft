package app.softwork.cikraft.gradle

import com.google.cloud.tools.jib.gradle.JibExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
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
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(JibFeature::class)
abstract class JibFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("jib", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<JibDefinition, BuildModel.None, JvmApplicationProjectType> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: JibDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationProjectType,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            pluginManager.apply("com.google.cloud.tools.jib")

            val jib = project.extensions.getByName("jib") as JibExtension
            jib.apply {
                from {
                    setImage(definition.from.image)
                    platforms {
                        for (platform in definition.from.platforms) {
                            platform {
                                architecture = platform.architecture.get()
                                os = platform.os.get()
                            }
                        }
                    }
                }
                to.setImage(definition.image)
                container.setMainClass(parentBuildModel.applications.getByName("main").mainClassName)
            }
        }
    }
}

interface JibDefinition : Definition<BuildModel.None> {
    @get:Nested
    val from: From
    val image: Property<String>
}

interface From {
    val image: Property<String>

    @get:Nested
    val platforms: NamedDomainObjectContainer<Platform>
}

interface Platform : Named {
    val architecture: Property<String>
    val os: Property<String>
}
