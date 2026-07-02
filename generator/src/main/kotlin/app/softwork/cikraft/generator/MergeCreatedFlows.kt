package app.softwork.cikraft.generator

import app.softwork.cikraft.core.CreatedFlow
import kotlinx.serialization.json.Json
import java.io.File

public fun mergeCreatedFlows(
    files: Collection<File>,
): String {
    val flows = buildList<CreatedFlow> {
        for (folder in files) {
            for (file in folder.walk()) {
                if (file.isFile) {
                    add(Json.decodeFromString(file.readText()))
                }
            }
        }
    }
    return Json.encodeToString(flows)
}
