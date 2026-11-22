import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.declarative.dsl.model.annotations.ElementFactoryName
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestingExtension
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

// https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/issues/50
@BindsProjectFeature(JvmTestSuiteFeature::class)
public abstract class JvmTestSuiteFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("testSuites", JvmTestSuiteFeatureAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
            .withNestedBuildModelImplementationType(
                JvmDclTestSuiteBuildModel::class.java,
                DefaultJvmDclTestSuiteBuildModel::class.java,
            ).withNestedBuildModelImplementationType(
                JvmDclTestSuiteTargetBuildModel::class.java,
                DefaultJvmDclTestSuiteTargetBuildModel::class.java,
            )
    }

    protected abstract class JvmTestSuiteFeatureAction :
        ProjectFeatureApplyAction<DclTestingExtension, BuildModel.None, JvmApplicationProjectType> {
        @get:Inject
        protected abstract val pluginManager: PluginManager

        @get:Inject
        protected abstract val tasks: TaskContainer

        @get:Inject
        protected abstract val project: Project

        @get:Inject
        protected abstract val providerFactory: ProviderFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DclTestingExtension,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationProjectType,
        ) {
            pluginManager.apply("jvm-test-suite")

            val testing = project.extensions.getByName("testing") as TestingExtension

            definition.suites.all { dclJvmSuite ->
                val buildModel = context.getBuildModel(dclJvmSuite)
                buildModel as DefaultJvmDclTestSuiteBuildModel

                val action: Action<JvmTestSuite> = Action { jvmTestSuite ->
                    jvmTestSuite.dependencies.implementation.bundle(
                        dclJvmSuite.dependencies.implementation.dependencies,
                    )
                    jvmTestSuite.dependencies.compileOnly.bundle(dclJvmSuite.dependencies.compileOnly.dependencies)
                    jvmTestSuite.dependencies.runtimeOnly.bundle(dclJvmSuite.dependencies.runtimeOnly.dependencies)
                    jvmTestSuite.dependencies.annotationProcessor.bundle(
                        dclJvmSuite.dependencies.annotationProcessor.dependencies,
                    )

                    dclJvmSuite.targets.all { dclTestSuiteTarget ->
                        val buildModel = context.getBuildModel(dclTestSuiteTarget)
                        buildModel as DefaultJvmDclTestSuiteTargetBuildModel
                        val action: Action<JvmTestSuiteTarget> = Action { jvmTestSuiteTarget ->
                            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { checkTask ->
                                // TaskProvider<Test> getTestTask(); is not supported in DCL, so we use this workaround for lifecycle task dependencies
                                val s = dclTestSuiteTarget.testing.dependsOnCheck.flatMap {
                                    if (it) {
                                        jvmTestSuiteTarget.testTask
                                    } else {
                                        providerFactory.present(emptyList<Nothing>())
                                    }
                                }.orElse(emptyList<Nothing>())
                                checkTask.dependsOn(s)
                            }

                            jvmTestSuiteTarget.testTask.configure { testTask ->
                                // JavaForkOptions uses Any/Object, that is not supported in DCL
                                // No provider api migration (yet), so there is no provider support, thus calling get
                                testTask.environment(dclTestSuiteTarget.testing.javaForkOptions.environment.get())
                            }
                        }
                        buildModel.target = if (dclTestSuiteTarget.name in jvmTestSuite.targets.names) {
                            jvmTestSuite.targets.getByName(dclTestSuiteTarget.name, action)
                        } else {
                            jvmTestSuite.targets.create(dclTestSuiteTarget.name, action)
                        }
                    }
                }
                buildModel.testSuite = if (dclJvmSuite.name in testing.suites.names) {
                    val jvmTestSuite = testing.suites.getByName(dclJvmSuite.name) as JvmTestSuite
                    action.execute(jvmTestSuite)
                    jvmTestSuite
                } else {
                    testing.suites.create(dclJvmSuite.name, JvmTestSuite::class.java, action)
                }
            }
        }
    }
}

// Can't reuse TestingExtension from core-api because of DomainObjectCollection<? extends TestSuiteTarget> getTargets();
// OUT/? extends is not (yet?) supported in DCL
public interface DclTestingExtension : Definition<BuildModel.None> {
    public val suites: NamedDomainObjectContainer<JvmDclTestSuite>
}

// Can't extend TestSuite from core-api because of DomainObjectCollection<? extends TestSuiteTarget> getTargets();
// OUT/? extends is not (yet?) supported in DCL
@ElementFactoryName("jvmTestSuite")
public interface JvmDclTestSuite :
    Definition<JvmDclTestSuiteBuildModel>,
    Named {
    public val targets: NamedDomainObjectContainer<JvmDclTestSuiteTarget>

    @get:Nested
    public val dependencies: JvmComponentDependencies
}

public interface JvmDclTestSuiteBuildModel : BuildModel {
    public val testSuite: JvmTestSuite
}

internal abstract class DefaultJvmDclTestSuiteBuildModel : JvmDclTestSuiteBuildModel {
    override lateinit var testSuite: JvmTestSuite
}

@ElementFactoryName("jvmTestSuiteTarget")
public interface JvmDclTestSuiteTarget :
    Named,
    Definition<JvmDclTestSuiteTargetBuildModel> {
    @get:Nested
    public val testing: TestingSpec

    // https://github.com/gradle/gradle/issues/36410
    // override fun getBinaryResultsDirectory(): DirectoryProperty
}

public interface JvmDclTestSuiteTargetBuildModel : BuildModel {
    public val target: JvmTestSuiteTarget
}
internal abstract class DefaultJvmDclTestSuiteTargetBuildModel : JvmDclTestSuiteTargetBuildModel {
    override lateinit var target: JvmTestSuiteTarget
}

public interface TestingSpec {
    public val dependsOnCheck: Property<Boolean>

    // JavaForkOptions uses Any/Object, that is not supported in DCL
    @get:Nested
    public val javaForkOptions: JavaDclForkOptions
}

public interface JavaDclForkOptions {
    public val environment: MapProperty<String, String>
}
