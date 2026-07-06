import com.sap.gateway.ip.core.customdev.util.Message

Message foo(Message message) {
  return CiKraftEntrypointsKt.foo(message, messageLogFactory.getMessageLog(message))
}
