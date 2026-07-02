package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface ApiRuntimeProvider : Named {
    @get:Input
    val title: Property<String>

    @get:Input
    val credentialStoreName: Property<String>

    @get:Input
    val credentialName: Property<String>
}
