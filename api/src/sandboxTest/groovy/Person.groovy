import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.msglog.MessageLog

class PersonConstructor {
    String name
    Integer age

    PersonConstructor(name, age) {
        this.name = name
        this.age = age
    }
}

Message logic(Message message) {
    MessageLog messageLog = messageLogFactory.getMessageLog(message)
    String correlationID = message.getHeader("X-Correlation-ID", String)
    String acceptHeader = message.getHeader("Accept", String)
    String accept
    if (acceptHeader != null) {
        accept = "ACCEPT " + acceptHeader
    } else {
        accept = "NO ACCEPT HEADER"
    }
    String contentTypeHeader = message.getHeader("Content-Type", String)
    String contentType
    if (contentTypeHeader != null) {
        contentType = "Content-Type " + contentTypeHeader
    } else {
        contentType = "NO Content-Type HEADER"
    }
    PersonConstructor person = message.getProperty("person") as PersonConstructor
    String query = message.getHeader("CamelHttpQuery", String)
    String method = message.getHeader("CamelHttpMethod", String)
    String path = message.getHeader("CamelHttpPath", String)
    message.body = person.name + " " + accept + " " + contentType + " " + query + " " + method + " " + path
    if (correlationID != null) {
        messageLog.addCustomHeaderProperty("X-Correlation-ID", correlationID)
    }
    String foo = null
    message.setHeader("X-FOO", foo)
    return message
}

static Message client(Message message) {
    message.setProperty("_RESULT_", new PersonConstructor('Marie', 1))
    return message
}
