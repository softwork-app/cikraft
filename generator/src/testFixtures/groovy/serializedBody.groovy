import com.sap.gateway.ip.core.customdev.util.Message

Message serialized(Message message) {
  return Entrypoints.serialized(message, messageLogFactory.getMessageLog(message))
}
