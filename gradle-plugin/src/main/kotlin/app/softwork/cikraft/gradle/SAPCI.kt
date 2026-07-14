package app.softwork.cikraft.gradle

import org.gradle.api.*
import org.gradle.api.attributes.*

interface SAPCI : Named {
    companion object {
        val attribute: Attribute<SAPCI> = Attribute.of("app.softwork.cikraft", SAPCI::class.java)

        const val JSON_ENTRYPOINTS = "entrypoints-json"
        const val API = "api"
        const val STAGES = "stages"
        const val OPENAPI = "openApi"
        const val STAGE_PROPERTIES = "stageProperties"
    }
}

const val SAPCI_USAGE = "sap-ci"

const val SAPCI_JVM_TARGET = 8
internal const val SAPCI_JVM_TARGET_STRING = "JVM_1_8"

interface SAPCIStage : Named {
    companion object {
        val attribute: Attribute<SAPCIStage> = Attribute.of("app.softwork.cikraft.stage", SAPCIStage::class.java)
    }
}
