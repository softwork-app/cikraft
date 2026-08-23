import com.intellij.rt.coverage.report.XMLCoverageReport
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.NormalizeLineEndings
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@CacheableTask
abstract class CoberturaReporterTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    @get:NormalizeLineEndings
    abstract val koverXmlReport: RegularFileProperty

    init {
        // https://github.com/gradle/gradle/issues/2919
        onlyIf { koverXmlReport.get().asFile.exists() }
    }

    @get:OutputFile
    abstract val coberturaXmlReport: RegularFileProperty

    @get:Classpath
    abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun execute() {
        workerExecutor.classLoaderIsolation {
            this.classpath.from(workerClasspath)
        }.submit(CoberturaReporterWorkAction::class.java) {
            this.koverXmlReport.set(this@CoberturaReporterTask.koverXmlReport)
            this.coberturaXmlReport.set(this@CoberturaReporterTask.coberturaXmlReport)
        }
    }
}

interface CoberturaReporterWorkAction : WorkAction<CoberturaReporterWorkAction.Params> {
    interface Params : WorkParameters {
        val koverXmlReport: RegularFileProperty
        val coberturaXmlReport: RegularFileProperty
    }

    @OptIn(ExperimentalTime::class)
    override fun execute() {
        parameters.koverXmlReport.get().asFile.inputStream().use {
            val coverageReport = XMLCoverageReport().read(it)

            val lineRate = coverageReport.classes.sumOf {
                it.coveredLines.toDouble()
            } / coverageReport.classes.sumOf { it.coveredLines + it.missedLines }

            println("Line rate $lineRate ${coverageReport.classes.sumOf {
                it.coveredLines.toDouble()
            }} / ${coverageReport.classes.sumOf { it.coveredLines + it.missedLines } }")

            val coverage = Coverage(
                lineRate = lineRate,
                branchRate = coverageReport.classes.sumOf {
                    it.coveredBranches.toDouble()
                } / coverageReport.classes.sumOf { it.coveredBranches + it.missedBranches },
                linesCovered = coverageReport.classes.sumOf {
                    it.coveredLines.toLong()
                },
                linesValid = coverageReport.classes.sumOf { it.coveredLines.toLong() + it.missedLines },
                branchesCovered = coverageReport.classes.sumOf {
                    it.coveredBranches.toLong()
                },
                branchesValid = coverageReport.classes.sumOf { it.coveredBranches.toLong() + it.missedBranches },
                complexity = 0.0,
                version = "kover-cobertura 1.0.0",
                timestamp = Clock.System.now(),
                sources = null,
                packages = Packages(),
            )

            parameters.coberturaXmlReport.asFile.get().writeText(
                XML.v1.encodeToString(Coverage.serializer(), coverage),
            )
        }
    }
}
