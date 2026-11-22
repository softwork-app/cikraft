package app.softwork.cikraft.gradle

import org.gradle.api.*
import org.gradle.api.attributes.*

public interface SAPCI : Named {
    public companion object {
        public val attribute: Attribute<SAPCI> = Attribute.of("app.softwork.cikraft", SAPCI::class.java)

        public const val JSON_ENTRYPOINTS: String = "entrypoints-json"
        public const val API: String = "api"
        public const val STAGES: String = "stages"
        public const val OPENAPI: String = "openApi"
        public const val OPENAPI_PROXY: String = "openApiProxy"
        public const val STAGE_PROPERTIES: String = "stageProperties"
    }
}

public const val SAPCI_USAGE: String = "sap-ci"

public const val SAPCI_JVM_TARGET: Int = 8
internal const val SAPCI_JVM_TARGET_STRING = "JVM_1_8"
