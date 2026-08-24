import com.sap.gateway.ip.core.customdev.util.Message

Message fooSuspend(Message message) {
  return CiKraftEntrypointsKt.fooSuspend(messageLogFactory.getMessageLog(message), message)
}
