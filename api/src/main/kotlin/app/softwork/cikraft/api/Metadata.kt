package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Metadata(
    val type: String? = null,
    val id: String? = null,
    val uri: String? = null,
    @SerialName("content_type")
    val contentType: String? = null,
    @SerialName("media_src")
    val mediaSrc: String? = null,
    @SerialName("edit_media")
    val editMedia: String? = null,
)
