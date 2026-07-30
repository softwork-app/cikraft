package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

interface Stage : Named {
    val httpServer: Property<String>

    val description: Property<String>

    val web: Property<String>

    @get:Nested
    val apiVirtualHosts: NamedDomainObjectContainer<ApiVirtualHost>
}

interface ApiVirtualHost : Named {
    val id: Property<String>
    val apiHttpServer: Property<String>
}
