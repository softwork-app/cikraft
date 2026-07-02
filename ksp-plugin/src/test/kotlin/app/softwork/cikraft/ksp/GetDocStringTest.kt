package app.softwork.cikraft.ksp

import kotlin.test.Test
import kotlin.test.assertEquals

class GetDocStringTest {
    @Test
    fun getDocString() {
        assertEquals(
            "a message",
            """Some Fault
 @param input a message
""".doc("input"),
        )
    }
}
