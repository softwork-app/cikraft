package app.softwork.cikraft.integrationflow.builder

import Stage
import app.softwork.cikraft.integrationflow.Config

public interface CreatedFlowConfig : Config {
    override val allowedHeaders: MutableSet<String>
    public val parameters: MutableMap<String, (Stage) -> Any>
    public val suffix: String?
}
