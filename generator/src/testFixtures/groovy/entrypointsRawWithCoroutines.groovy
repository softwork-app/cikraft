import com.sap.gateway.ip.core.customdev.util.Message

Message rawSuspend(Message message) {
  return Entrypoints.rawSuspend(message, messageLogFactory.getMessageLog(message))
}
