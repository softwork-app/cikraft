import com.sap.gateway.ip.core.customdev.util.Message

static Message processData(Message message) {
    message.setHeader("Content-Type", "application/xml")

    boolean found = message.getHeader("SAP_DatastoreEntryFound", String).toBoolean()
    if (found) {
        message.setHeader("CamelHttpResponseCode", 200)
    } else {
        message.setHeader("CamelHttpResponseCode", 404)
    }
    return message
}
