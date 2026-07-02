import com.sap.it.api.msglog.*
import java.util.*

class MockMessageLog : MessageLog {
    private val _properties = mutableMapOf<String, Any>()
    val properties: Map<String, Any> = _properties

    data class Attachment(val text: String, val mediaType: String)

    private val _attachments = mutableMapOf<String, Attachment>()
    val attachments: Map<String, Attachment> = _attachments

    private val _headers = mutableMapOf<String, String>()
    val headers: Map<String, String> = _headers

    override fun setStringProperty(name: String, value: String) {
        _properties[name] = value
    }

    override fun setIntegerProperty(name: String, value: Int) {
        _properties[name] = value
    }

    override fun setLongProperty(name: String, value: Long) {
        _properties[name] = value
    }

    override fun setBooleanProperty(name: String, value: Boolean) {
        _properties[name] = value
    }

    override fun setFloatProperty(name: String, value: Float) {
        _properties[name] = value
    }

    override fun setDoubleProperty(name: String, value: Double) {
        _properties[name] = value
    }

    override fun setDateProperty(name: String, value: Date) {
        _properties[name] = value
    }

    override fun addAttachmentAsString(name: String, text: String, mediaType: String) {
        _attachments[name] = Attachment(text, mediaType)
    }

    override fun addCustomHeaderProperty(name: String, value: String) {
        _headers[name] = value
    }
}
