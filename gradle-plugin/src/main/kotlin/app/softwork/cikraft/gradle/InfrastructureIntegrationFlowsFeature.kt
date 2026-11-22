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
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.internal.component.external.model.ProjectDerivedCapability
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.util.internal.TextUtil
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import javax.inject.Inject

@BindsProjectFeature(InfrastructureIntegrationFlowsFeature::class)
public abstract class InfrastructureIntegrationFlowsFeature :
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

    protected abstract class ApplyAction :
        ProjectFeatureApplyAction<SAPCIIFlowsDefinition, SAPCIIFlowsBuildModel, SAPCIInfrastructureDefinition> {
        @get:Inject
        protected abstract val configurations: ConfigurationRegistrar

        @get:Inject
        protected abstract val configurationContainer: ConfigurationContainer

        @get:Inject
        protected abstract val tasks: TaskRegistrar

        @get:Inject
        protected abstract val layout: ProjectFeatureLayout

        @get:Inject
        protected abstract val dependencyFactory: DependencyFactory

        @get:Inject
        protected abstract val project: ProjectInternal

        @get:Inject
        protected abstract val sourceSets: SourceSetContainer

        @get:Inject
        protected abstract val objectFactory: ObjectFactory

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
            buildModel.stages.addAllLater(parentBuildModel.apiStages.elements)
            buildModel.stages.addAllLater(parentBuildModel.transportStages.elements)
            buildModel.integrationPackages.addAll(definition.integrationPackages)

            val projectVersion = project.version.toString().takeUnless { it == Project.DEFAULT_VERSION }

            val apiWorker = configurations.dependencyScope("apiWorker") {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:api:$VERSION"))
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:core:$VERSION"))
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
                it.dependencies.add(dependencyFactory.create(KTOR_CIO))
                it.dependencies.add(dependencyFactory.create(KTOR_LOGGING))
            }
            val apiWorkerClasspath = configurations.resolvable("apiWorkerClasspath") {
                it.extendsFrom(apiWorker)
            }

            val generator = configurations.dependencyScope("generatorWorker") {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val generatorClasspath = configurations.resolvable("generatorWorkerClasspath") {
                it.extendsFrom(generator)
            }

            definition.dependencies.implementation.add("app.softwork.cikraft:integration-flow-builder-runtime:$VERSION")

            val integrationFlowsSourceSet = sourceSets.create("integrationFlows") {
                configurationContainer.named(it.implementationConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.implementation)
                }
                configurationContainer.named(it.runtimeOnlyConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.runtimeOnly)
                }
                configurationContainer.named(it.compileOnlyConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.compileOnly)
                }
                configurationContainer.named(it.annotationProcessorConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.annotationProcessor)
                }
            }

            val integrationFlowsSourceSetJar = tasks.register(integrationFlowsSourceSet.name + "Jar", Jar::class.java)
            integrationFlowsSourceSetJar.configure {
                it.description = "Assembles a jar archive containing the classes of the 'integration-flow' feature."
                it.group = BasePlugin.BUILD_GROUP
                it.from(integrationFlowsSourceSet.output)
                it.archiveClassifier.set(TextUtil.camelToKebabCase("integrationFlows"))
            }

            configurations.consumable("integrationFlowsRuntimeElements") {
                it.attributes {
                    it.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        it.named(Usage::class.java, Usage.JAVA_RUNTIME),
                    )
                    it.attribute(
                        Category.CATEGORY_ATTRIBUTE,
                        it.named(Category::class.java, Category.LIBRARY),
                    )
                    it.attribute(
                        LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                        it.named(LibraryElements::class.java, LibraryElements.JAR),
                    )
                    it.attribute(
                        Bundling.BUNDLING_ATTRIBUTE,
                        it.named(Bundling::class.java, Bundling.EXTERNAL),
                    )
                }
                it.outgoing {
                    it.capability(ProjectDerivedCapability(project, "integrationFlows"))
                    it.artifact(integrationFlowsSourceSetJar)
                    it.attributes.attribute(
                        ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                        ArtifactTypeDefinition.JAR_TYPE,
                    )
                }
            }

            val generateStages = tasks.register("generateStages", CreateSAPCIStagesEnum::class.java) {
                val enumStages = parentBuildModel.apiStages.elements.zip(
                    parentBuildModel.transportStages.elements,
                ) { apiStages, transportStages ->
                    val stages = apiStages + transportStages
                    stages.map { stage ->
                        objectFactory.newInstance(EnumStage::class.java, stage.name).apply {
                            stageDescription.set(stage.description)
                            httpServer.set(
                                stage.httpServer.zip(buildModel.httpSuffix) { server, suffix ->
                                    server + suffix
                                },
                            )
                            web.set(stage.web)
                        }
                    }
                }
                it.stages.addAllLater(enumStages)

                it.classpath.from(generatorClasspath)
            }
            integrationFlowsSourceSet.kotlin.srcDir(generateStages)

            val entrypointsJsonProjectDeps = configurations.dependencyScope("cikraftEntrypointsDeps") {
                it.dependencies.add(dependencyFactory.createProjectDependency())
            }
            val entrypointsJsonProject = configurations.resolvable("cikraftEntrypoints") {
                it.extendsFrom(entrypointsJsonProjectDeps)
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.JSON_ENTRYPOINTS))
                }
            }

            val generateTypedKotlinScriptAccessors = tasks.register(
                "generateTypedKotlinFlowAccessor",
                GenerateTypedKotlinStepBuilderTask::class.java,
            ) {
                it.jsonScriptEntry.from(entrypointsJsonProject)
                it.workerClasspath.from(generatorClasspath)
            }
            integrationFlowsSourceSet.kotlin.srcDirs(generateTypedKotlinScriptAccessors)

            val createInfrastructureDryRunOutputFolder =
                layout.contextBuildDirectory.map { it.dir("cikraft/flows/dryrun") }

            val createInfrastructureDryRun = tasks.register(
                "createInfrastructureDryRun",
                CreateInfrastructureDryRun::class.java,
            ) {
                it.stageNames.addAll(parentBuildModel.apiStages.names)
                it.stageNames.addAll(parentBuildModel.transportStages.names)

                it.outputFolder.convention(createInfrastructureDryRunOutputFolder)
                it.classpath.from(
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
                    it.stageName.set(stage.name)
                    it.apiServer.set(stage.apiServer)
                    it.authServer.set(stage.authServer)

                    it.workerClasspath.from(apiWorkerClasspath)

                    it.suffix.convention(parentBuildModel.suffix)
                }
            }

            definition.integrationPackages.all { integrationPackage ->
                parentBuildModel.apiStages.all { stage ->
                    tasks.register(
                        "create${integrationPackage.name}On${stage.name}",
                        CreateIntegrationPackageTask::class.java,
                        stage.name,
                    ).configure {
                        it.apiServer.set(stage.apiServer)
                        it.authServer.set(stage.authServer)

                        it.packageName.set(integrationPackage.name)
                        it.packageID.set(integrationPackage.name.replace("_", ""))
                        it.packageVersion.set(projectVersion)
                        it.packageDescription.set(integrationPackage.description)

                        it.workerClasspath.from(apiWorkerClasspath)
                    }
                }
                parentBuildModel.transportStages.all { transportStage ->
                    transportStage.apiSourceStages.all { apiSourceStage ->
                        tasks.register(
                            "transport${integrationPackage.name}To${transportStage.name}From${apiSourceStage.name}",
                        ).configure {
                            it.group = "cikraft"
                        }
                    }
                }

                integrationPackage.integrationFlows.all { integrationFlowM ->
                    val iFlowBuildModel = context.getBuildModel(integrationFlowM)
                    iFlowBuildModel as DefaultIntegrationFlowBuildModel
                    iFlowBuildModel.internalName = integrationFlowM.name
                    iFlowBuildModel.packageName = integrationPackage.name
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
                    iFlowBuildModel.autoNumbering.set(integrationFlowM.autoNumbering)

                    iFlowBuildModel.dependenciesJars.from(
                        configurations.resolvable(
                            "cikraft${iFlowBuildModel.name}ProjectJars",
                        ) {
                            it.fromDependencyCollector(integrationFlowM.dependencies.implementation)
                            it.attributes {
                                it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, Usage.JAVA_RUNTIME))
                            }
                        },
                    )

                    val entrypointsJson = configurations.resolvable("cikraft${iFlowBuildModel.name}Entrypoints") {
                        it.fromDependencyCollector(integrationFlowM.dependencies.implementation)
                        it.attributes {
                            it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                            it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.JSON_ENTRYPOINTS))
                        }
                    }

                    val generateGroovyEntrypoints = tasks.register(
                        "generate${iFlowBuildModel.name}GroovyEntrypoints",
                        GenerateGroovyEntryPoints::class.java,
                    ) {
                        it.groovyEntryPoints.convention(
                            layout.contextBuildDirectory.map {
                                it.file(
                                    "cikraft/${iFlowBuildModel.name}/entrypoints.groovy",
                                )
                            },
                        )
                        it.scripts.from(entrypointsJson)
                        it.workerClasspath.from(generatorClasspath)
                    }

                    iFlowBuildModel.generatedScripts.from(generateGroovyEntrypoints)

                    val generateKotlinEntryPointsTask = tasks.register(
                        "generate${iFlowBuildModel.name}KotlinEntrypoints",
                        CreateKotlinEntryPoints::class.java,
                    ) {
                        it.kotlinEntryPoints.convention(
                            layout.contextBuildDirectory.map {
                                it.dir(
                                    "cikraft/${iFlowBuildModel.name}/entrypoints/kotlin",
                                )
                            },
                        )
                        it.scripts.from(entrypointsJson)
                        it.workerClasspath.from(generatorClasspath)
                    }

                    val kotlinExtension = project.extensions.getByName("kotlin") as KotlinBaseExtension
                    val kotlinJvmExtension = kotlinExtension as KotlinJvmExtension

                    val compileKotlinEntrypointCompilation = kotlinJvmExtension.target.compilations.create(
                        iFlowBuildModel.name.replace("_", ""),
                    ) {
                        it as KotlinWithJavaCompilation<*, *>
                        it.defaultSourceSet.kotlin.srcDir(generateKotlinEntryPointsTask)
                        it.compileTaskProvider.configure {
                            (it.compilerOptions as KotlinJvmCompilerOptions).jvmTarget.set(
                                JvmTarget.valueOf(SAPCI_JVM_TARGET_STRING),
                            )
                        }
                        it.compileJavaTaskProvider.configure {
                            it.options.release.set(SAPCI_JVM_TARGET)
                        }
                        configurationContainer.getByName(it.implementationConfigurationName) {
                            it.fromDependencyCollector(integrationFlowM.dependencies.implementation)
                            it.extendsFrom(configurationContainer.getByName("implementation"))
                            it.dependencies.add(dependencyFactory.create(SAPCI_SCRIPT_API))
                            it.dependencies.add(dependencyFactory.create(SAPCI_GENERIC_API))
                            it.dependencies.add(dependencyFactory.create(SAPCI_CAMEL))
                            it.dependencies.add(dependencyFactory.create(SAPCI_ACTIVATION))
                        }
                    }

                    iFlowBuildModel.kotlinEntryPointsClasses.from(compileKotlinEntrypointCompilation.output.classesDirs)

                    val jar = tasks.register("cikraft" + iFlowBuildModel.name + "Jar", Jar::class.java) {
                        it.from(iFlowBuildModel.kotlinEntryPointsClasses)
                        it.destinationDirectory.set(
                            layout.contextBuildDirectory.map {
                                it.dir("cikraft/${iFlowBuildModel.name}")
                            },
                        )
                        it.archiveFileName.set("entrypoints.jar")
                    }

                    iFlowBuildModel.libs.from(jar, iFlowBuildModel.dependenciesJars)

                    val generateTypedKotlinIntegrationFlowBuilder = tasks.register(
                        "generateFlowAccessor${iFlowBuildModel.name}",
                        GenerateTypedKotlinIntegrationFlowBuilderTask::class.java,
                    ) {
                        it.jsonScriptEntry.from(entrypointsJson)
                        it.packageName.set(integrationPackage.name)
                        it.packageDescription.set(integrationPackage.description)
                        it.flowName.set(iFlowBuildModel.name)
                        it.flowDescription.set(iFlowBuildModel.description)
                        it.flowSource.addAll(iFlowBuildModel.source)
                        it.flowTarget.addAll(iFlowBuildModel.target)

                        it.suffix.set(parentBuildModel.suffix)
                        it.baseUrl.set(buildModel.httpSuffix)
                        it.workerClasspath.from(generatorClasspath)
                        it.groovyScripts.from(iFlowBuildModel.scripts)
                        it.autoNumbering.set(iFlowBuildModel.autoNumbering)
                    }

                    integrationFlowsSourceSet.kotlin.srcDirs(generateTypedKotlinIntegrationFlowBuilder)

                    createInfrastructureDryRun.configure {
                        it.expectedRawIFlowsIds.add(iFlowBuildModel.name.replace("_", ""))
                        it.entryPoints.from(entrypointsJson)
                    }

                    val generateTypedKotlinScriptAccessors = tasks.register(
                        "generateTypedKotlinFlowAccessor${iFlowBuildModel.name}",
                        GenerateTypedKotlinStepBuilderTask::class.java,
                    ) {
                        it.jsonScriptEntry.from(entrypointsJson)
                        it.workerClasspath.from(generatorClasspath)
                    }
                    integrationFlowsSourceSet.kotlin.srcDirs(generateTypedKotlinScriptAccessors)

                    val generateTypeGroovyScriptAccessors = tasks.register(
                        "generateTypedGroovyFlowAccessor${iFlowBuildModel.name}",
                        GenerateTypedGroovyStepBuilderTask::class.java,
                    ) {
                        it.groovyScripts.from(iFlowBuildModel.scripts)
                        it.workerClasspath.from(generatorClasspath)
                    }

                    integrationFlowsSourceSet.kotlin.srcDirs(generateTypeGroovyScriptAccessors)

                    parentBuildModel.apiStages.all { stage ->
                        val stageName = stage.name.replaceFirstChar { it.uppercase() }

                        val uploadIFlow = tasks.register(
                            "upload${iFlowBuildModel.name}On$stageName",
                            UploadIFlow::class.java,
                            stage.name,
                        )
                        uploadIFlow.configure {
                            it.dependsOn("create${integrationPackage.name}On$stageName")
                            it.libs.from(iFlowBuildModel.libs)
                            it.scripts.from(iFlowBuildModel.scripts)
                            it.scripts.from(iFlowBuildModel.generatedScripts)
                            it.flowXmlDefinition.set(
                                createInfrastructureDryRun.flatMap { it.outputFolder }
                                    .map { it.file("definitions/${iFlowBuildModel.name}.xml") },
                            )

                            it.apiServer.set(stage.apiServer)
                            it.authServer.set(stage.authServer)

                            it.packageID.set(integrationPackage.name.replace("_", ""))

                            it.flowName.set(
                                parentBuildModel.suffix.orElse("").map { suffix ->
                                    if (suffix.isBlank()) {
                                        iFlowBuildModel.name
                                    } else {
                                        val suffixID = suffix.replace("/", "_").uppercase()
                                        "${iFlowBuildModel.name}$suffixID"
                                    }
                                },
                            )
                            it.flowID.set(iFlowBuildModel.flowID)
                            it.flowDescription.set(iFlowBuildModel.description)
                            it.flowSource.set(iFlowBuildModel.source)
                            it.flowTarget.set(iFlowBuildModel.target)
                            it.parametersFile.set(
                                createInfrastructureDryRun.flatMap { it.outputFolder }
                                    .map { it.file("properties/$stageName/${iFlowBuildModel.name}.properties") },
                            )

                            it.workerClasspath.from(apiWorkerClasspath)
                            it.version.set(projectVersion)
                        }

                        val deployIFlow = tasks.register(
                            "deploy${iFlowBuildModel.name}On${stageName}Infrastructure",
                            DeployIFlow::class.java,
                            stage.name,
                        )
                        deployIFlow.configure {
                            it.dependsOn("upload${iFlowBuildModel.name}On${stage.name}")

                            it.apiServer.set(stage.apiServer)
                            it.authServer.set(stage.authServer)

                            it.flowID.set(iFlowBuildModel.flowID)
                            it.version.set(projectVersion)

                            it.workerClasspath.from(apiWorkerClasspath)
                        }

                        deployToStage[stage]!!.configure {
                            it.dependsOn(deployIFlow)
                        }
                        undeployToStage[stage]!!.configure {
                            it.flowIds.add(iFlowBuildModel.name)
                        }
                    }
                    parentBuildModel.transportStages.all { transportStage ->
                        val transportStageName = transportStage.name
                        transportStage.apiSourceStages.all { apiSourceStage ->
                            val stage = parentBuildModel.apiStages.named(apiSourceStage.name)

                            val service = project.gradle.sharedServices.registerIfAbsent(
                                "cleanTransportPackage" + integrationPackage.name.replace(
                                    "_",
                                    "",
                                ) + apiSourceStage.name,
                                TransportLockBuildService::class.java,
                            ) {
                                it.parameters {
                                    it.packageID.set(integrationPackage.name.replace("_", ""))
                                    it.apiSourceStage.set(apiSourceStage.name)

                                    it.apiServer.set(stage.flatMap { it.apiServer })
                                    it.authServer.set(stage.flatMap { it.authServer })
                                    it.packageName.set(integrationPackage.name)
                                    it.packageDescription.set(integrationPackage.description)
                                    it.packageVersion.set(projectVersion)
                                    it.workerClasspath.from(apiWorkerClasspath)
                                }
                            }

                            val uploadIFlow = tasks.register(
                                "upload${iFlowBuildModel.name}On${apiSourceStage.name}ForTransportTo$transportStageName",
                                UploadIFlow::class.java,
                                apiSourceStage.name,
                            )

                            uploadIFlow.configure {
                                it.libs.from(iFlowBuildModel.libs)
                                it.scripts.from(iFlowBuildModel.scripts)
                                it.scripts.from(iFlowBuildModel.generatedScripts)
                                it.flowXmlDefinition.set(
                                    createInfrastructureDryRun.flatMap { it.outputFolder }
                                        .map { it.file("definitions/${iFlowBuildModel.name}.xml") },
                                )

                                it.lockService.set(service)

                                it.apiServer.set(stage.flatMap { it.apiServer })
                                it.authServer.set(stage.flatMap { it.authServer })

                                it.packageID.set(integrationPackage.name.replace("_", ""))

                                it.flowName.set(
                                    parentBuildModel.suffix.orElse("").map { suffix ->
                                        if (suffix.isBlank()) {
                                            iFlowBuildModel.name
                                        } else {
                                            val suffixID = suffix.replace("/", "_").uppercase()
                                            "${iFlowBuildModel.name}$suffixID"
                                        }
                                    },
                                )
                                it.flowID.set(iFlowBuildModel.flowID)
                                it.flowDescription.set(iFlowBuildModel.description)
                                it.flowSource.set(iFlowBuildModel.source)
                                it.flowTarget.set(iFlowBuildModel.target)
                                it.parametersFile.set(
                                    createInfrastructureDryRun.flatMap { it.outputFolder }
                                        .map {
                                            it.file(
                                                "properties/$transportStageName/${iFlowBuildModel.name}.properties",
                                            )
                                        },
                                )

                                it.workerClasspath.from(apiWorkerClasspath)
                                it.version.set(projectVersion)
                            }

                            val transportIPTask = project.tasks.named(
                                "transport${integrationPackage.name}To${transportStageName}From${apiSourceStage.name}",
                            )

                            transportIPTask.configure {
                                it.dependsOn(uploadIFlow)
                            }
                        }
                    }
                }
            }

            configurations.consumable("aggregatedDryRun") {
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.API))
                }
                it.outgoing {
                    val createdFlowsArtifact = objectFactory.newInstance(
                        LazyDirectoryArtifact::class.java,
                        ArtifactTypeDefinition.DIRECTORY_TYPE,
                        "createdFlows",
                        createInfrastructureDryRun,
                        createInfrastructureDryRunOutputFolder.map {
                            it.dir("flows")
                        },
                    )
                    it.artifact(createdFlowsArtifact)
                }
            }

            configurations.consumable("cikraftProperties") {
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.STAGE_PROPERTIES))
                }
                it.outgoing {
                    val propertiesStageArtifact = objectFactory.newInstance(
                        LazyDirectoryArtifact::class.java,
                        "properties",
                        "properties",
                        createInfrastructureDryRun,
                        createInfrastructureDryRunOutputFolder.map {
                            it.dir("properties")
                        },
                    )
                    it.artifact(propertiesStageArtifact)
                }
            }
        }
    }
}

internal val SourceSet.kotlin get() = extensions.getByName("kotlin") as org.gradle.api.file.SourceDirectorySet

@BindsProjectFeature(InfrastructureIntegrationFlowsR8Feature::class)
public abstract class InfrastructureIntegrationFlowsR8Feature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        @Suppress("INVISIBLE_REFERENCE")
        builder.bindProjectFeature("r8", ApplyAction::class)
            .withBuildModelImplementationType(io.github.hfhbd.r8.DefaultR8BuildModel::class.java)
            .withUnsafeApplyAction()
    }

    protected abstract class ApplyAction : ProjectFeatureApplyAction<R8Definition, R8BuildModel, IntegrationFlow> {
        @get:Inject
        protected abstract val tasks: TaskRegistrar

        @get:Inject
        protected abstract val configurations: ConfigurationRegistrar

        @get:Inject
        protected abstract val layout: ProjectFeatureLayout

        @get:Inject
        protected abstract val dependencyFactory: DependencyFactory

        @get:Inject
        protected abstract val javaToolchainService: JavaToolchainService

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
                it.fromDependencyCollector(buildModel.r8DependencyCollector)
            }

            val libs = layout.contextBuildDirectory.map { it.dir("cikraft/${iFlowBuildModel.name}/libs") }

            val sapciRuntimeLibs = configurations.dependencyScope("sapciRuntimeLibs${iFlowBuildModel.name}") {
                it.dependencies.add(dependencyFactory.create(SAPCI_SCRIPT_API))
                it.dependencies.add(dependencyFactory.create(SAPCI_GENERIC_API))
                it.dependencies.add(dependencyFactory.create(SAPCI_CAMEL))
                it.dependencies.add(dependencyFactory.create(SAPCI_ACTIVATION))
            }

            val r8LibJars = configurations.resolvable("r8LibJars${iFlowBuildModel.name}") {
                it.extendsFrom(sapciRuntimeLibs)
            }

            val r8Jar = tasks.register("r8Jar${iFlowBuildModel.name}", R8JarTask::class.java) {
                it.r8Jar.set(libs.map { it.file("r8.jar") })
                it.additionalRules.addAll(
                    "-dontobfuscate",
                    "-allowaccessmodification",
                    "-keepattributes SourceFile, LineNumberTable",
                    "-keep,allowoptimization public class CiKraftEntrypointsKt { <methods>; }",
                )
                it.additionalRules.addAll(buildModel.additionalRules)
                it.programFiles.from(iFlowBuildModel.kotlinEntryPointsClasses, iFlowBuildModel.dependenciesJars)
                it.libJars.from(r8LibJars)
                it.javaHome.set(
                    javaToolchainService.launcherFor {
                        it.languageVersion.set(JavaLanguageVersion.of(SAPCI_JVM_TARGET))
                    }.map { it.metadata.installationPath },
                )
                it.r8Classpath.setFrom(r8ClasspathConfig)
            }

            iFlowBuildModel.libs.setFrom(r8Jar)
        }
    }
}
