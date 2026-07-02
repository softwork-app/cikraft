import com.sap.gateway.ip.core.customdev.util.Message

Message fooSuspend(Message message) {
  return Entrypoints.fooSuspend(message, messageLogFactory.getMessageLog(message))
}
