package com.example

import app.softwork.cikraft.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlinx.serialization.json.*

@ContentType("application/json", parameters = ["charset=utf-8"])
object JsonFactory: StringFormat by Json
