import com.sap.gateway.ip.core.customdev.util.Message

Message typed(Message message) {
  return CiKraftEntrypointsKt.typed(message, messageLogFactory.getMessageLog(message))
}
