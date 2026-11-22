package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer

public interface TransportStage : Stage {
    public val apiSourceStages: NamedDomainObjectContainer<ApiSourceStage>
}

public interface ApiSourceStage : Named
