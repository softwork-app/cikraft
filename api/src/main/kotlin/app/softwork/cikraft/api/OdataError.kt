package app.softwork.cikraft.api

import kotlinx.serialization.Serializable

@Serializable
public data class OdataError(val error: ErrorDetails) : Throwable(error.message.value) {
    override val message: String = error.message.value

    @Serializable
    public data class ErrorDetails(val code: String, val message: Message) {
        @Serializable
        public data class Message(val lang: String, val value: String)
    }
}
