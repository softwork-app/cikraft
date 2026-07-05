import com.sap.gateway.ip.core.customdev.util.Message

Message noError(Message message) {
  return CiKraftEntrypointsKt.noError(message, messageLogFactory.getMessageLog(message))
}
