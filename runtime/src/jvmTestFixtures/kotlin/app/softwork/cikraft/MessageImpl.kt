package app.softwork.cikraft

import com.sap.gateway.ip.core.customdev.util.AttachmentWrapper
import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.gateway.ip.core.customdev.util.SoapHeader
import org.apache.camel.Attachment
import javax.activation.DataHandler

/**
 * A fake implementation to test the actual code used in SAP CI.
 * The [getBodySize] and [getAttachmentsSize] are not yet implemented.
 *
 * @see <a href="https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/en-US/index.html">SAP CI Message JavaDoc</a>
 *
 * @see <a href="https://help.sap.com/docs/cloud-integration/sap-cloud-integration/headers-and-exchange-properties-provided-by-integration-framework" a>Available Properties</a>
 */
class MessageImpl(
    private var body: Any?,
    properties: Map<String, Any> = emptyMap(),
    attachments: Map<String, DataHandler> = emptyMap(),
    headers: Map<String, Any> = emptyMap(),
    soapHeaders: List<SoapHeader> = emptyList(),
) : Message {
    override fun getBodySize(): Nothing = error("Not yet supported")
    override fun getAttachmentsSize(): Nothing = error("Not yet supported")

    private var properties = properties.toMutableMap()
    private var attachments = attachments.toMutableMap()
    private var headers = headers.toMutableMap()
    private var soapHeaders = soapHeaders.toMutableList()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBody(klass: Class<T>): T? = body as T?
    override fun getBody(): Any? = body
    override fun setBody(newBody: Any?) {
        body = newBody
    }

    override fun getAttachments(): Map<String, DataHandler> = attachments
    override fun setAttachments(newAttachments: Map<String, DataHandler>) {
        attachments = newAttachments.toMutableMap()
    }

    override fun getHeaders(): Map<String, Any> = headers
    override fun <T : Any> getHeader(header: String, klass: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return headers[header] as T?
    }

    override fun setHeaders(newHeaders: Map<String, Any>) {
        headers = newHeaders.toMutableMap()
    }

    override fun setHeader(header: String, value: Any) {
        headers[header] = value
    }

    override fun getProperties(): Map<String, Any> = properties

    override fun setProperties(newProperties: Map<String, Any>) {
        properties = newProperties.toMutableMap()
    }

    override fun setProperty(key: String, value: Any) {
        properties[key] = value
    }

    override fun getProperty(key: String): Any? = properties[key]

    override fun addAttachmentHeader(headerName: String, headerValue: String, attachment: AttachmentWrapper): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated(
        "Use AttachmentWrapper",
        replaceWith = ReplaceWith("addAttachmentHeader(headerName, headerValue, attachment"),
    )
    override fun addAttachmentHeader(headerName: String, headerValue: String, attachment: Attachment): Nothing {
        TODO("Not yet implemented")
    }

    override fun setAttachmentHeader(headerName: String, headerValue: String, attachment: AttachmentWrapper): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated(
        "Use AttachmentWrapper",
        replaceWith = ReplaceWith("setAttachmentHeader(headerName, headerValue, attachment"),
    )
    override fun setAttachmentHeader(headerName: String, headerValue: String, attachment: Attachment): Nothing {
        TODO("Not yet implemented")
    }

    override fun getAttachmentHeader(headerName: String, attachment: AttachmentWrapper): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated("Use AttachmentWrapper", replaceWith = ReplaceWith("getAttachmentHeader(headerName, attachment"))
    override fun getAttachmentHeader(headerName: String, attachment: Attachment): Nothing {
        TODO("Not yet implemented")
    }

    override fun removeAttachmentHeader(headerName: String, attachment: AttachmentWrapper): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated("Use AttachmentWrapper", replaceWith = ReplaceWith("removeAttachmentHeader(headerName, attachment"))
    override fun removeAttachmentHeader(headerName: String, attachment: Attachment): Nothing {
        TODO("Not yet implemented")
    }

    override fun getAttachmentWrapperObjects(): Nothing {
        TODO("Not yet implemented")
    }

    override fun setAttachmentWrapperObjects(newAttachmentObjects: Map<String, AttachmentWrapper>): Nothing {
        TODO("Not yet implemented")
    }

    override fun addAttachmentObject(id: String, content: AttachmentWrapper): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated("Use AttachmentWrapper", replaceWith = ReplaceWith("addAttachmentObject(id, content"))
    override fun addAttachmentObject(id: String, content: Attachment): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated("Use getAttachmentWrapperObjects", replaceWith = ReplaceWith("getAttachmentWrapperObjects"))
    override fun getAttachmentObjects(): Nothing {
        TODO("Not yet implemented")
    }

    @Deprecated(
        "Use AttachmentWrappers",
        replaceWith = ReplaceWith("setAttachmentWrapperObjects(newAttachmentObjects)"),
    )
    override fun setAttachmentObjects(newAttachmentObjects: Map<String, Attachment>): Nothing {
        TODO("Not yet implemented")
    }

    override fun getSoapHeaders(): List<SoapHeader> = soapHeaders

    override fun setSoapHeaders(newSoapHeaders: List<SoapHeader>) {
        soapHeaders = newSoapHeaders.toMutableList()
    }

    /**
     * removes all SOAP headers of the current message
     */
    override fun clearSoapHeaders() {
        soapHeaders.clear()
    }
}
