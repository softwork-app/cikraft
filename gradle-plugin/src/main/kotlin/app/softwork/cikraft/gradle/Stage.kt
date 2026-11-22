package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

public interface Stage : Named {
    public val httpServer: Property<String>

    public val description: Property<String>

    public val web: Property<String>

    public val apiVirtualHosts: NamedDomainObjectContainer<ApiVirtualHost>
}

/**
 * The name must be the same across stages
 */
public interface ApiVirtualHost : Named {
    public val id: Property<String>
    public val apiHttpServer: Property<String>
    public val clientAuthEnabled: Property<Boolean>
}
