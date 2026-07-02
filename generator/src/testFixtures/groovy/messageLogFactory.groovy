import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.msglog.MessageLog
import com.sap.it.api.msglog.MessageLogFactory

/**
 * Mock class to compile the tests
 */
class messageLogFactory implements MessageLogFactory {
    static MessageLog getMessageLog(Message m) {
        return null
    }

    @Override
    MessageLog getMessageLog(Object o) {
        return new MockMessageLog()
    }
}
