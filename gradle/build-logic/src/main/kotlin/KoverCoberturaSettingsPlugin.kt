import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named

abstract class KoverCoberturaSettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.gradle.lifecycle.beforeProject {
            if (this == rootProject) {
                pluginManager.apply("reporting-base")
                val reportingBaseExtension = extensions.getByName<ReportingExtension>(ReportingExtension.NAME)

                @Suppress("INVISIBLE_REFERENCE")
                val koverXmlReportTask = tasks.named("koverXmlReport", kotlinx.kover.gradle.aggregation.settings.tasks.KoverXmlReportTask::class)
                @Suppress("INVISIBLE_REFERENCE")
                val xmlReportFile = koverXmlReportTask.flatMap { it.reportFile }

                val ds = configurations.dependencyScope("koverCoberturaXmlReportDependencies") {
                    dependencies.add(dependencyFactory.create("org.jetbrains.intellij.deps:intellij-coverage-reporter:1.0.777"))
                    dependencies.add(dependencyFactory.create("io.github.pdvrieze.xmlutil:serialization:1.0.1"))
                }

                val koverCoberturaClasspath = configurations.resolvable("koverCoberturaXmlReportClasspath") {
                    extendsFrom(ds)
                }

                tasks.register("koverCoberturaXmlReport", CoberturaReporterTask::class.java) {
                    koverXmlReport.set(xmlReportFile)
                    coberturaXmlReport.convention(reportingBaseExtension.baseDirectory.file("kover/cobertura.xml"))
                    workerClasspath.from(koverCoberturaClasspath)
                }
            }
        }
    }
}
