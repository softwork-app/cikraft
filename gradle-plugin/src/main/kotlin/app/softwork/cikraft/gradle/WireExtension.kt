package app.softwork.cikraft.gradle

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

abstract class WireExtension @Inject constructor(private val task: TaskProvider<out Task>) {
    fun useSourceSet(sourceSet: SourceSet) {
        sourceSet.kotlin.srcDir(task)
    }

    fun useSourceSet(
        sourceSetProvider: NamedDomainObjectProvider<out SourceSet>,
    ) {
        sourceSetProvider.configure {
            useSourceSet(this)
        }
    }

    private val SourceSet.kotlin get() = (this as ExtensionAware).extensions.getByName<SourceDirectorySet>("kotlin")
}
