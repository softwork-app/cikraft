import com.sap.gateway.ip.core.customdev.util.Message

Message noOutputs(Message message) {
  return Entrypoints.noOutputs(message, messageLogFactory.getMessageLog(message))
}
