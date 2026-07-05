import app.softwork.cikraft.*
import kotlin.test.*

class EntrypointsTest {
    @Test
    fun acceptHeaderWithoutProperties() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "application/json",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun noAcceptHeader() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun acceptHeaderWithProperties() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json;charset=utf-8",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "application/json;charset=utf-8",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun acceptHeaderWithMultiProperties() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json; charset=utf-8; foo=bar",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "application/json; charset=utf-8; foo=bar",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun multipleAcceptHeaderWithMultiProperties() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/problem+json; charset=utf-8; foo=bar, application/json; charset=utf-8; foo=bar",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "application/problem+json; charset=utf-8; foo=bar, application/json; charset=utf-8; foo=bar",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun acceptStar() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "*/*",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(null, message.headers[STATUS_CODE_HEADER])
        assertEquals(
            """{
    "x": 84
}""",
            message.body,
        )
        assertEquals(mapOf("_RESULT_" to SerializedOutput(b = B(x = 84))), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "*/*",
                "B" to "some request header",
            ),
            message.headers,
        )
    }

    @Test
    fun wrongAccept() {
        val message = MessageImpl(
            body = """{"x": "42"}""",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "text/plain",
                "B" to "some request header",
            ),
        )
        val messageLog = MockMessageLog()
        message.serialized(messageLog = messageLog)
        assertEquals(406, message.headers[STATUS_CODE_HEADER])
        assertEquals(null, message.body)
        assertEquals(mapOf(), message.properties)
        assertEquals(
            mapOf(
                "Content-Type" to "application/json",
                "Accept" to "text/plain",
                "B" to "some request header",
                "CamelHttpResponseCode" to 406,
            ),
            message.headers,
        )
    }
}
