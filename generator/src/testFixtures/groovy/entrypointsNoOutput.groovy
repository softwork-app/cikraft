import com.sap.gateway.ip.core.customdev.util.Message

Message noOutputs(Message message) {
  return CiKraftEntrypointsKt.noOutputs(messageLogFactory.getMessageLog(message), message)
}
