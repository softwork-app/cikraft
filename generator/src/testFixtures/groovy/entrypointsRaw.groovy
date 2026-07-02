import com.sap.gateway.ip.core.customdev.util.Message

Message raw(Message message) {
  return Entrypoints.raw(message, messageLogFactory.getMessageLog(message))
}
