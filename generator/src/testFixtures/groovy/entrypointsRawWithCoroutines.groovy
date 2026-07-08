import com.sap.gateway.ip.core.customdev.util.Message

Message rawSuspend(Message message) {
  return CiKraftEntrypointsKt.rawSuspend(message, messageLogFactory.getMessageLog(message))
}
