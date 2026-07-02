package app.softwork.cikraft.gradle

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.artifacts.publish.AbstractPublishArtifact
import org.gradle.api.internal.tasks.TaskDependencyFactory
import org.gradle.api.provider.Provider
import java.io.File
import java.util.*
import javax.inject.Inject

internal abstract class LazyDirectoryArtifact @Inject constructor(
    private val type: String,
    private val name: String,
    producer: Any,
    private val fileProvider: Provider<FileSystemLocation>,
    taskDependencyFactory: TaskDependencyFactory,
) : AbstractPublishArtifact(taskDependencyFactory, producer) {
    override fun getName(): String = name
    override fun getExtension(): String = ""
    override fun getType(): String = type
    override fun getClassifier(): String? = null
    override fun getDate(): Date? = null
    override fun shouldBePublished(): Boolean = false
    override fun getFile(): File = fileProvider.get().asFile
}
