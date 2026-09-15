package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.provider.Property

interface ApiRuntimeProvider : Named {
    val title: Property<String>
    val credentialStoreName: Property<String>
    val credentialName: Property<String>
}
