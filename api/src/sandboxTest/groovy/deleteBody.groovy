import com.sap.gateway.ip.core.customdev.util.Message

static Message processData(Message message) {
    message.setHeader("CamelHttpResponseCode", "201")

    String location = switchEndpoint(message.getHeader("CamelHttpUrl", String), "write", "get")
    message.setHeader("Location", location)

    message.setBody(null)
    return message
}

static String switchEndpoint(String url, String from, String to) {
    URI uri = new URI(url)
    String newPath = uri.path.replaceFirst("/${from}/", "/${to}/")
    URI newUri = new URI('https', uri.authority, newPath, uri.query, uri.fragment)
    return newUri.toString()
}
