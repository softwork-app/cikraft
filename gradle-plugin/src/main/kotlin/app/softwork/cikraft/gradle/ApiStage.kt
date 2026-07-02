package app.softwork.cikraft.gradle

import org.gradle.api.provider.Property

interface ApiStage : Stage {
    val apiServer: Property<String>
    val authServer: Property<String>

    val apiPortalServer: Property<String>
}
