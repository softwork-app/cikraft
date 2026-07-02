import com.sap.gateway.ip.core.customdev.util.Message

Message foo(Message message) {
  return Entrypoints.foo(message, messageLogFactory.getMessageLog(message))
}
