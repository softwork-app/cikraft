package app.softwork.cikraft.gradle

import io.github.hfhbd.r8.R8BuildModel
import io.github.hfhbd.r8.R8Definition
import io.github.hfhbd.r8.R8JarTask
import io.github.hfhbd.r8.R8_MODULE
import io.github.hfhbd.r8.R8_VERSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import javax.inject.Inject

@BindsProjectFeature(InfrastructureIntegrationFlowsFeature::class)
abstract class InfrastructureIntegrationFlowsFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("integrationArtifacts", ApplyAction::class)
            .withUnsafeApplyAction()
            .withBuildModelImplementationType(DefaultSAPCIIFlowsBuildModel::class.java)
            .withNestedBuildModelImplementationType(
                IntegrationFlowBuildModel::class.java,
                DefaultIntegrationFlowBuildModel::class.java,
            )
            .withUnsafeDefinition()
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<SAPCIIFlowsDefinition, SAPCIIFlowsBuildModel, SAPCIInfrastructureDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val configurationContainer: ConfigurationContainer

        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val sourceSets: SourceSetContainer

        @get:Inject
        abstract val objectFactory: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIIFlowsDefinition,
            buildModel: SAPCIIFlowsBuildModel,
            parentDefinition: SAPCIInfrastructureDefinition,
        ) {
            buildModel as DefaultSAPCIIFlowsBuildModel
            val parentBuildModel = context.getBuildModel(parentDefinition)
            buildModel.suffix.set(parentBuildModel.suffix)
            buildModel.httpSuffix.set(parentBuildModel.httpSuffix)
            buildModel.stages.addAll(parentDefinition.apiStages)
            buildModel.stages.addAll(parentDefinition.transportStages)
            buildModel.integrationPackages.addAll(definition.integrationPackages)

            val apiWorker = configurations.dependencyScope("apiWorker") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:api:$VERSION"))
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:core:$VERSION"))
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
                dependencies.add(dependencyFactory.create(KTOR_CIO))
                dependencies.add(dependencyFactory.create(KTOR_LOGGING))
            }
            val apiWorkerClasspath = configurations.resolvable("apiWorkerClasspath") {
                extendsFrom(apiWorker)
            }

            val generator = configurations.dependencyScope("generatorWorker") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val generatorClasspath = configurations.resolvable("generatorWorkerClasspath") {
                extendsFrom(generator)
            }

            val integrationFlowsSourceSet = sourceSets.create("integrationFlows") {
                configurationContainer.named(implementationConfigurationName) {
                    dependencies.add(
                        dependencyFactory.create("app.softwork.cikraft:integration-flow-builder-runtime:$VERSION"),
                    )
                }
            }

            val generateStages = tasks.register("generateStages", CreateSAPCIStagesEnum::class.java) {
                val enumStages = project.provider { buildModel.stages }.map { stages ->
                    stages.map { stage ->
                        objectFactory.newInstance<EnumStage>(stage.name).apply {
                            stageDescription.set(stage.description)
                            httpServer.set(
                                stage.httpServer.zip(buildModel.httpSuffix) { server, suffix ->
                                    server + suffix
                                },
                            )
                            apiHttpServer.set(
                                stage.apiHttpServer.zip(buildModel.httpSuffix) { server, suffix ->
                                    server + suffix
                                },
                            )
                            web.set(stage.web)
                        }
                    }
                }
                stages.addAllLater(enumStages)

                classpath.from(generatorClasspath)
            }
            integrationFlowsSourceSet.kotlin.srcDir(generateStages)

            val createInfrastructureDryRunOutputFolder =
                layout.contextBuildDirectory.map { it.dir("cikraft/flows/dryrun") }

            val createInfrastructureDryRun = tasks.register(
                "createInfrastructureDryRun",
                CreateInfrastructureDryRun::class.java,
            ) {
                this.stageNames.set(buildModel.stages.names)
                this.outputFolder.convention(createInfrastructureDryRunOutputFolder)
                classpath.from(
                    integrationFlowsSourceSet.kotlin.classesDirectory,
                    integrationFlowsSourceSet.runtimeClasspath,
                    apiWorkerClasspath,
                )
            }

            val deployToStage = parentBuildModel.apiStages.associateWith { stage ->
                val taskName = stage.name.replaceFirstChar { it.uppercase() }
                tasks.register("deploy${taskName}Infrastructure")
            }

            val undeployToStage = parentBuildModel.apiStages.associateWith { stage ->
                val taskName = stage.name.replaceFirstChar { it.uppercase() }

                tasks.register("undeploy${taskName}Infrastructure", UndeployInfrastructure::class.java) {
                    this.stageName.set(stage.name)
                    this.apiServer.set(stage.apiServer)
                    this.authServer.set(stage.authServer)

                    workerClasspath.from(apiWorkerClasspath)

                    suffix.convention(parentBuildModel.suffix)
                }
            }

            definition.integrationPackages.all {
                val integrationPackage = this

                parentBuildModel.apiStages.all {
                    val stage = this
                    tasks.register(
                        "create${integrationPackage.name}On${stage.name}",
                        CreateIntegrationPackageTask::class.java,
                        stage.name,
                    ).configure {
                        this.apiServer.set(stage.apiServer)
                        this.authServer.set(stage.authServer)

                        this.packageName.set(integrationPackage.name)
                        this.packageID.set(integrationPackage.name.replace("_", ""))
                        this.packageDescription.set(integrationPackage.description)

                        workerClasspath.from(apiWorkerClasspath)
                    }
                }

                this.integrationFlows.all {
                    val integrationFlowM = this
                    val iFlowBuildModel = context.getBuildModel(integrationFlowM)
                    iFlowBuildModel as DefaultIntegrationFlowBuildModel
                    iFlowBuildModel.internalName = integrationFlowM.name
                    iFlowBuildModel.flowID.set(
                        parentBuildModel.suffix.orElse("").map { suffix ->
                            val flowID = iFlowBuildModel.name.replace("_", "")
                            if (suffix.isBlank()) {
                                flowID
                            } else {
                                val suffixID = suffix.replace("/", "").uppercase()
                                "$flowID$suffixID"
                            }
                        },
                    )

                    iFlowBuildModel.description.set(integrationFlowM.description)
                    iFlowBuildModel.source.set(integrationFlowM.source)
                    iFlowBuildModel.target.set(integrationFlowM.target)
                    iFlowBuildModel.scripts.from(integrationFlowM.scripts)

                    iFlowBuildModel.dependenciesJars.from(
                        configurations.resolvable(
                            "cikraft${iFlowBuildModel.name}ProjectJars",
                        ) {
                            fromDependencyCollector(integrationFlowM.dependencies.implementation)
                            attributes {
                                attribute(Usage.USAGE_ATTRIBUTE, named(Usage.JAVA_RUNTIME))
                            }
                        },
                    )

                    val entrypointsJson = configurations.resolvable("cikraft${iFlowBuildModel.name}Entrypoints") {
                        fromDependencyCollector(integrationFlowM.dependencies.implementation)
                        attributes {
                            attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                            attribute(SAPCI.attribute, named(SAPCI.JSON_ENTRYPOINTS))
                        }
                    }

                    val generateGroovyEntrypoints = tasks.register(
                        "generate${iFlowBuildModel.name}GroovyEntrypoints",
                        GenerateGroovyEntryPoints::class.java,
                    ) {
                        this.groovyEntryPoints.convention(
                            layout.contextBuildDirectory.map {
                                it.file(
                                    "cikraft/${iFlowBuildModel.name}/entrypoints.groovy",
                                )
                            },
                        )
                        this.scripts.from(entrypointsJson)
                        this.workerClasspath.from(generatorClasspath)
                    }

                    iFlowBuildModel.generatedScripts.from(generateGroovyEntrypoints)

                    val generateKotlinEntryPointsTask = tasks.register(
                        "generate${iFlowBuildModel.name}KotlinEntrypoints",
                        CreateKotlinEntryPoints::class.java,
                    ) {
                        kotlinEntryPoints.convention(
                            layout.contextBuildDirectory.map {
                                it.dir(
                                    "cikraft/${iFlowBuildModel.name}/entrypoints/kotlin",
                                )
                            },
                        )
                        this.scripts.from(entrypointsJson)
                        this.workerClasspath.from(generatorClasspath)
                    }

                    val kotlinExtension = project.extensions.getByName("kotlin") as KotlinBaseExtension
                    val kotlinJvmExtension = kotlinExtension as KotlinJvmExtension

                    val compileKotlinEntrypointCompilation = kotlinJvmExtension.target.compilations.create(
                        iFlowBuildModel.name.replace("_", ""),
                    ) {
                        this as KotlinWithJavaCompilation<*, *>
                        defaultSourceSet.kotlin.srcDir(generateKotlinEntryPointsTask)
                        compileTaskProvider.configure {
                            (compilerOptions as KotlinJvmCompilerOptions).jvmTarget.set(
                                JvmTarget.valueOf(SAPCI_JVM_TARGET_STRING),
                            )
                        }
                        compileJavaTaskProvider.configure {
                            options.release.set(SAPCI_JVM_TARGET)
                        }
                        configurationContainer.getByName(implementationConfigurationName) {
                            fromDependencyCollector(integrationFlowM.dependencies.implementation)
                            extendsFrom(configurationContainer.getByName("implementation"))
                            dependencies.add(dependencyFactory.create(SAPCI_SCRIPT_API))
                            dependencies.add(dependencyFactory.create(SAPCI_GENERIC_API))
                            dependencies.add(dependencyFactory.create(SAPCI_CAMEL))
                            dependencies.add(dependencyFactory.create(SAPCI_ACTIVATION))
                        }
                    }

                    iFlowBuildModel.kotlinEntryPointsClasses.from(compileKotlinEntrypointCompilation.output.classesDirs)

                    val jar = tasks.register("cikraft" + iFlowBuildModel.name + "Jar", Jar::class.java) {
                        from(iFlowBuildModel.kotlinEntryPointsClasses)
                        destinationDirectory.set(
                            layout.contextBuildDirectory.map {
                                it.dir("cikraft/${iFlowBuildModel.name}")
                            },
                        )
                        archiveFileName.set("entrypoints.jar")
                    }

                    iFlowBuildModel.libs.from(jar, iFlowBuildModel.dependenciesJars)

                    val generatedTypedKotlinFlows: TaskProvider<GenerateTypedKotlinFlow> = tasks.register(
                        "generateFlowAccessor$name",
                        GenerateTypedKotlinFlow::class.java,
                    ) {
                        this.jsonScriptEntry.from(entrypointsJson)
                        this.packageName.set(integrationPackage.name)
                        this.packageDescription.set(integrationPackage.description)
                        this.flowName.set(iFlowBuildModel.name)
                        this.flowDescription.set(iFlowBuildModel.description)
                        this.flowSource.addAll(iFlowBuildModel.source)
                        this.flowTarget.addAll(iFlowBuildModel.target)

                        suffix.set(parentBuildModel.suffix)
                        this.baseUrl.set(buildModel.httpSuffix)
                        this.workerClasspath.from(generatorClasspath)
                        this.groovyScripts.from(iFlowBuildModel.scripts)
                    }

                    integrationFlowsSourceSet.kotlin.srcDirs(generatedTypedKotlinFlows)

                    createInfrastructureDryRun.configure {
                        entryPoints.from(entrypointsJson)
                    }

                    val projectVersion = project.version.toString().takeUnless { it == Project.DEFAULT_VERSION }

                    parentBuildModel.apiStages.all {
                        val stage = this
                        val stageName = stage.name.replaceFirstChar { it.uppercase() }

                        val deployIFlow = tasks.register(
                            "deploy${iFlowBuildModel.name}On${stageName}Infrastructure",
                            DeployIFlow::class.java,
                            stage.name,
                        )
                        deployIFlow.configure {
                            dependsOn("create${integrationPackage.name}On${stage.name}")
                            this.libs.from(iFlowBuildModel.libs)
                            this.scripts.from(iFlowBuildModel.scripts)
                            this.scripts.from(iFlowBuildModel.generatedScripts)
                            this.flowXmlDefinition.set(
                                createInfrastructureDryRun.flatMap { it.outputFolder }
                                    .map { it.file("definitions/${iFlowBuildModel.name}.xml") },
                            )

                            this.apiServer.set(stage.apiServer)
                            this.authServer.set(stage.authServer)

                            this.packageID.set(integrationPackage.name.replace("_", ""))

                            this.flowName.set(
                                parentBuildModel.suffix.orElse("").map { suffix ->
                                    if (suffix.isBlank()) {
                                        iFlowBuildModel.name
                                    } else {
                                        val suffixID = suffix.replace("/", "_").uppercase()
                                        "${iFlowBuildModel.name}$suffixID"
                                    }
                                },
                            )
                            this.flowID.set(iFlowBuildModel.flowID)
                            this.flowDescription.set(iFlowBuildModel.description)
                            this.flowSource.set(iFlowBuildModel.source)
                            this.flowTarget.set(iFlowBuildModel.target)
                            this.parametersFile.set(
                                createInfrastructureDryRun.flatMap { it.outputFolder }
                                    .map { it.file("properties/$stageName/${iFlowBuildModel.name}.properties") },
                            )

                            workerClasspath.from(apiWorkerClasspath)
                            this.version.set(projectVersion)
                        }

                        deployToStage[this]!!.configure {
                            dependsOn(deployIFlow)
                        }
                        undeployToStage[this]!!.configure {
                            flowIds.add(iFlowBuildModel.name)
                        }
                    }
                }
            }

            configurations.consumable("aggregatedDryRun") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.API))
                }
                outgoing {
                    val createdFlowsArtifact = objectFactory.newInstance(
                        LazyDirectoryArtifact::class,
                        ArtifactTypeDefinition.DIRECTORY_TYPE,
                        "createdFlows",
                        createInfrastructureDryRun,
                        createInfrastructureDryRunOutputFolder.map {
                            it.dir("flows")
                        },
                    )
                    artifact(createdFlowsArtifact)
                }
            }

            buildModel.stages.all {
                val stageName = name
                configurations.consumable("properties$stageName") {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                        attribute(SAPCI.attribute, named(SAPCI.STAGE_PROPERTIES))
                        attribute(SAPCIStage.attribute, named(stageName))
                    }
                    outgoing {
                        val propertiesStageArtifact = objectFactory.newInstance(
                            LazyDirectoryArtifact::class,
                            "properties",
                            "properties",
                            createInfrastructureDryRun,
                            createInfrastructureDryRunOutputFolder.map {
                                it.dir("properties/$stageName")
                            },
                        )
                        artifact(propertiesStageArtifact)
                    }
                }
            }
        }
    }
}

internal val SourceSet.kotlin get() = extensions.getByName("kotlin") as org.gradle.api.file.SourceDirectorySet

@BindsProjectFeature(InfrastructureIntegrationFlowsR8Feature::class)
abstract class InfrastructureIntegrationFlowsR8Feature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        @Suppress("INVISIBLE_REFERENCE")
        builder.bindProjectFeature("r8", ApplyAction::class)
            .withBuildModelImplementationType(io.github.hfhbd.r8.DefaultR8BuildModel::class.java)
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<R8Definition, R8BuildModel, IntegrationFlow> {
        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val javaToolchainService: JavaToolchainService

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: R8Definition,
            buildModel: R8BuildModel,
            parentDefinition: IntegrationFlow,
        ) {
            val iFlowBuildModel = context.getBuildModel(parentDefinition)

            buildModel.r8Version.set(definition.r8Version.orElse(R8_VERSION))
            buildModel.additionalRules.set(definition.additionalRules)

            @Suppress("INVISIBLE_REFERENCE")
            buildModel as io.github.hfhbd.r8.DefaultR8BuildModel
            buildModel.r8DependencyCollector.add(
                buildModel.r8Version.map { r8Version ->
                    dependencyFactory.create("$R8_MODULE:$r8Version")
                },
            )

            val r8ClasspathConfig = configurations.resolvable("r8Classpath${iFlowBuildModel.name}") {
                fromDependencyCollector(buildModel.r8DependencyCollector)
            }

            val libs = layout.contextBuildDirectory.map { it.dir("cikraft/${iFlowBuildModel.name}/libs") }

            val sapciRuntimeLibs = configurations.dependencyScope("sapciRuntimeLibs${iFlowBuildModel.name}") {
                dependencies.add(dependencyFactory.create(SAPCI_SCRIPT_API))
                dependencies.add(dependencyFactory.create(SAPCI_GENERIC_API))
                dependencies.add(dependencyFactory.create(SAPCI_CAMEL))
                dependencies.add(dependencyFactory.create(SAPCI_ACTIVATION))
            }

            val r8LibJars = configurations.resolvable("r8LibJars${iFlowBuildModel.name}") {
                extendsFrom(sapciRuntimeLibs)
            }

            val r8Jar = tasks.register("r8Jar${iFlowBuildModel.name}", R8JarTask::class.java) {
                this.r8Jar.set(libs.map { it.file("r8.jar") })
                this.additionalRules.addAll(
                    "-dontobfuscate",
                    "-allowaccessmodification",
                    "-keepattributes SourceFile, LineNumberTable",
                    "-keep,allowoptimization public class CiKraftEntrypointsKt { <methods>; }",
                )
                this.programFiles.from(iFlowBuildModel.kotlinEntryPointsClasses, iFlowBuildModel.dependenciesJars)
                this.libJars.from(r8LibJars)
                this.javaHome.set(
                    javaToolchainService.launcherFor {
                        languageVersion.set(JavaLanguageVersion.of(SAPCI_JVM_TARGET))
                    }.map { it.metadata.installationPath },
                )
                this.r8Classpath.setFrom(r8ClasspathConfig)
            }

            iFlowBuildModel.libs.setFrom(r8Jar)
        }
    }
}
