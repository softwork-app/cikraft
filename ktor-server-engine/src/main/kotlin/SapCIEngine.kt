import com.sap.gateway.ip.core.customdev.util.*
import io.ktor.events.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.io.*
import java.io.*
import kotlin.coroutines.*

public class SapCIEngine(
    environment: ApplicationEnvironment,
    monitor: Events,
    developmentMode: Boolean = true,
    private val application: () -> Application,
) : BaseApplicationEngine(environment, monitor, developmentMode) {
    override fun start(wait: Boolean): SapCIEngine = this

    override fun stop(gracePeriodMillis: Long, timeoutMillis: Long) {}

    public suspend fun handle(message: Message): Message {
        val call = SAPCIApplicationCall(
            coroutineContext = currentCoroutineContext(),
            application = application(),
            message = message,
        )

        try {
            pipeline.execute(call)
        } catch (error: Throwable) {
            handleFailure(call, error)
        } finally {
            call.close()
        }
        return message
    }
}

private data class SAPCIRequestConnectionPoint(
    @Deprecated("Use localHost or serverHost instead")
    override val host: String,
    override val localAddress: String,
    override val localHost: String,
    override val localPort: Int,
    override val method: HttpMethod,
    @Deprecated("Use localPort or serverPort instead")
    override val port: Int,
    override val remoteAddress: String,
    override val remoteHost: String,
    override val remotePort: Int,
    override val scheme: String,
    override val serverHost: String,
    override val serverPort: Int,
    override val uri: String,
    override val version: String,
) : RequestConnectionPoint

private class SAPCIApplicationRequest(
    context: CoroutineContext,
    applicationCall: BaseApplicationCall,
    message: Message,
) : BaseApplicationRequest(applicationCall),
    AutoCloseable {
    val url = Url(message.getProperty("CamelHttpUrl") as String)

    private val job = Job()
    override val cookies: RequestCookies = RequestCookies(this)
    override val engineHeaders: Headers = Headers.build {
        for ((name, value) in message.headers) {
            append(name, value.toString())
        }
    }

    override val local: RequestConnectionPoint = SAPCIRequestConnectionPoint(
        host = url.host,
        localHost = url.host,
        serverHost = url.host,
        localAddress = url.host,
        port = url.port,
        serverPort = url.port,
        localPort = url.port,
        method = HttpMethod.parse(message.getProperty("CamelHttpMethod") as String),
        remoteAddress = "unknown",
        remoteHost = "unknown",
        remotePort = 0,
        scheme = url.protocol.name,
        uri = url.fullPath,
        version = "unknown",
    )
    override val queryParameters: Parameters =
        (message.getProperty("CamelHttpQuery") as String).parseUrlEncodedParameters()

    override val rawQueryParameters: Parameters = Parameters.build {
        val query = message.getProperty("CamelHttpQuery") as String
        val keyValues = query.split("&").map {
            it.substringBefore("=") to it.substringAfter("=", "")
        }
        for ((key, value) in keyValues) {
            append(key, value)
        }
    }

    override val engineReceiveChannel: ByteReadChannel =
        message.getBody(InputStream::class.java).toByteReadChannel(context + job)

    override fun close() {
        job.cancel()
    }
}

private class SAPCIApplicationCall(
    override val coroutineContext: CoroutineContext,
    application: Application,
    message: Message,
) : BaseApplicationCall(application),
    AutoCloseable {
    override val request: SAPCIApplicationRequest = SAPCIApplicationRequest(coroutineContext, this, message)
    override val response: SAPCIApplicationResponse = SAPCIApplicationResponse(this, message)

    init {
        putResponseAttribute()
    }

    override fun close() {
        request.close()
    }
}

private class SAPCIApplicationResponse(call: SAPCIApplicationCall, private val message: Message) :
    BaseApplicationResponse(call) {
    private val outputStream = ByteArrayOutputStream()
    private val rawSink = outputStream.asSink()

    init {
        message.body = outputStream
    }

    override val headers = object : ResponseHeaders() {
        override fun engineAppendHeader(name: String, value: String) {
            message.headers[name] = value
        }

        override fun getEngineHeaderNames(): List<String> = message.headers.keys.toList()

        override fun getEngineHeaderValues(name: String): List<String> = listOf(message.headers[name].toString())
    }

    override suspend fun respondUpgrade(upgrade: OutgoingContent.ProtocolUpgrade): Nothing = error("Not supported")

    override suspend fun responseChannel(): ByteWriteChannel = rawSink.asByteWriteChannel()

    override fun setStatus(statusCode: HttpStatusCode) {
        message.setHeader("CamelHttpResponseCode", statusCode.value)
    }
}

public data object SapCI : ApplicationEngineFactory<SapCIEngine, BaseApplicationEngine.Configuration> {
    override fun configuration(
        configure: BaseApplicationEngine.Configuration.() -> Unit,
    ): BaseApplicationEngine.Configuration = BaseApplicationEngine.Configuration().apply(configure)

    override fun create(
        environment: ApplicationEnvironment,
        monitor: Events,
        developmentMode: Boolean,
        configuration: BaseApplicationEngine.Configuration,
        applicationProvider: () -> Application,
    ): SapCIEngine = SapCIEngine(
        environment,
        monitor,
        developmentMode,
        applicationProvider,
    )
}

public suspend fun sapCIServer(message: Message, application: Application.() -> Unit): Message {
    val server = embeddedServer(SapCI, module = application)
    server.startSuspend(true)
    try {
        server.engine.handle(message)
    } finally {
        server.stop()
    }
    return message
}
