package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

interface IntegrationPackage : Named {
    val description: Property<String>

    @get:Nested
    val integrationFlows: NamedDomainObjectContainer<IntegrationFlow>
}
