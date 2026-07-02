package com.example

import app.softwork.cikraft.ContentType
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ContentType("application/json", parameters = ["charset=utf-8"])
data object JsonFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})

@ContentType("application/xml")
data object XmlFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})

@ContentType("application/octet-stream")
data object StreamFactory : StringFormat by Json {
    override val serializersModule: SerializersModule = EmptySerializersModule()
}
