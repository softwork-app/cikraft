import com.sap.gateway.ip.core.customdev.util.Message

Message serialized(Message message) {
  return CiKraftEntrypointsKt.serialized(messageLogFactory.getMessageLog(message), message)
}
