package app.softwork.cikraft.gradle

import org.gradle.api.provider.Property

public interface ApiStage : Stage {
    public val apiServer: Property<String>
    public val authServer: Property<String>

    public val apiPortalServer: Property<String>
}
