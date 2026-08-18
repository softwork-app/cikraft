package app.softwork.cikraft

import com.sap.it.api.msglog.*
import java.util.*

class MockMessageLog : MessageLog {
    val properties: Map<String, Any> field = mutableMapOf<String, Any>()

    data class Attachment(val text: String, val mediaType: String)
    val attachments: Map<String, Attachment> field = mutableMapOf<String, Attachment>()

    val headers: Map<String, String> field = mutableMapOf<String, String>()

    override fun setStringProperty(name: String, value: String) {
        properties[name] = value
    }

    override fun setIntegerProperty(name: String, value: Int) {
        properties[name] = value
    }

    override fun setLongProperty(name: String, value: Long) {
        properties[name] = value
    }

    override fun setBooleanProperty(name: String, value: Boolean) {
        properties[name] = value
    }

    override fun setFloatProperty(name: String, value: Float) {
        properties[name] = value
    }

    override fun setDoubleProperty(name: String, value: Double) {
        properties[name] = value
    }

    override fun setDateProperty(name: String, value: Date) {
        properties[name] = value
    }

    override fun addAttachmentAsString(name: String, text: String, mediaType: String) {
        attachments[name] = Attachment(text, mediaType)
    }

    override fun addCustomHeaderProperty(name: String, value: String) {
        headers[name] = value
    }
}
