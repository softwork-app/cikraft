import com.sap.gateway.ip.core.customdev.util.Message

Message typed(Message message) {
  return CiKraftEntrypointsKt.typed(messageLogFactory.getMessageLog(message), message)
}
