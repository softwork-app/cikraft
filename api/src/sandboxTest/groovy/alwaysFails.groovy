import com.sap.gateway.ip.core.customdev.util.Message

class FooException extends Exception {
    FooException(String message) {
        super(message)
    }
}

static Message processData(Message message) {
    String body = message.getBody(String)
    if (body == null || body.isEmpty()) {
        throw new FooException("Foo")
    } else {
        throw new IOException("Bar")
    }
}

static Message errorHandling(Message message) {
    Exception scriptException = message.getProperty("CamelExceptionCaught") as Exception
    Throwable nestedSapCIException = scriptException.cause
    Throwable realCause = nestedSapCIException.cause

    if (realCause != null && realCause instanceof FooException) {
        message.setBody(realCause.message)
        message.setHeader("CamelHttpResponseCode", 444)
    } else {
        throw realCause
    }
    return message
}
