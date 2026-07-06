import com.sap.gateway.ip.core.customdev.util.Message

Message raw(Message message) {
  return CiKraftEntrypointsKt.raw(message, messageLogFactory.getMessageLog(message))
}
