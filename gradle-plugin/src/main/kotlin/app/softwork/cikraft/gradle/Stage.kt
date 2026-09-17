package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

interface Stage : Named {
    val httpServer: Property<String>

    val description: Property<String>

    val web: Property<String>

    val apiVirtualHosts: NamedDomainObjectContainer<ApiVirtualHost>
}

/**
 * The name must be the same across stages
 */
interface ApiVirtualHost : Named {
    val id: Property<String>
    val apiHttpServer: Property<String>
    val clientAuthEnabled: Property<Boolean>
}
