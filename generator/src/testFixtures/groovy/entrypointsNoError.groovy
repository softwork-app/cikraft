import com.sap.gateway.ip.core.customdev.util.Message

Message noError(Message message) {
  return Entrypoints.noError(message, messageLogFactory.getMessageLog(message))
}
