import com.sap.gateway.ip.core.customdev.util.Message

static Message convert(Message message) {
    return ConvertKt.convert(message)
}

static Message error(Message message) {
    return ErrorKt.error(message)
}

static Message sendCSVToRVSoverAWSS3(Message message) {
    return RvsKt.sendCSVToRVSoverAWSS3(message)
}
