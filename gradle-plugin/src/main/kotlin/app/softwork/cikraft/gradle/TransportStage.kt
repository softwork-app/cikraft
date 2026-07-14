package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer

interface TransportStage : Stage {
    val apiSourceStages: NamedDomainObjectContainer<ApiSourceStage>
}

interface ApiSourceStage : Named
