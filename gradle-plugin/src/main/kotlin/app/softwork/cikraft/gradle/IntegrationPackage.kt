package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

public interface IntegrationPackage : Named {
    public val description: Property<String>

    public val integrationFlows: NamedDomainObjectContainer<IntegrationFlow>
}
