import com.sap.gateway.ip.core.customdev.util.Message

Message serialized(Message message) {
  return CiKraftEntrypointsKt.serialized(message, messageLogFactory.getMessageLog(message))
}
