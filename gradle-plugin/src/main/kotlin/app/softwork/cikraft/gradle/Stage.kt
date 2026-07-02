package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.provider.Property

interface Stage : Named {
    val httpServer: Property<String>
    val apiHttpServer: Property<String>

    val description: Property<String>

    val web: Property<String>

    val apiVirtualHost: Property<String>
}
