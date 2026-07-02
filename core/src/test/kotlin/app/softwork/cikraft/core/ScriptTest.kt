package app.softwork.cikraft.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ScriptTest {
    @Test
    fun testPackageName() {
        assertEquals("", getPackageName("FooKt.foo"))
        assertEquals("com.example", getPackageName("com.example.FooKt.foo"))
    }
}
