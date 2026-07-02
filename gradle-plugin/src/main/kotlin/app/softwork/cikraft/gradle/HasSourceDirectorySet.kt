package app.softwork.cikraft.gradle

import org.gradle.api.file.SourceDirectorySet
import org.gradle.features.binding.BuildModel

interface HasSourceDirectorySet : BuildModel {
    val sourceDirectorySet: SourceDirectorySet
}
