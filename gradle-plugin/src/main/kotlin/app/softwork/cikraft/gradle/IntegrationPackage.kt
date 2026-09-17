package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

interface IntegrationPackage : Named {
    val description: Property<String>

    val integrationFlows: NamedDomainObjectContainer<IntegrationFlow>
}
