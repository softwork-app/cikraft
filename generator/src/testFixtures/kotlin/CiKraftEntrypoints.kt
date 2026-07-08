import com.example.FooInput
import com.example.FooOutput2
import com.example.JsonFactory
import com.example.StreamFactory
import com.example.binaryRedirect
import com.example.core.Fault
import com.example.foo
import com.example.fooSuspend
import com.example.javaStreams
import com.example.kotlinxIO
import com.example.noError
import com.example.noOutputs
import com.example.raw
import com.example.rawSuspend
import com.example.setup
import com.example.twoPart1
import com.example.twoPart2
import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.ITApiFactory
import com.sap.it.api.keystore.KeystoreService
import com.sap.it.api.msglog.MessageLog
import com.sap.it.api.securestore.SecureStoreService
import java.io.InputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import javax.naming.InitialContext
import javax.sql.DataSource
import kotlin.Boolean
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.text.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

public fun Message.foo(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
      requestFactory = JsonFactory
    }
    else -> {
      setHeader("Accept-Post", "application/json")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val output = foo(body = requestFactory.decodeFromString(FooInput.serializer(), getBody(String::class.java)),
        b = getHeader("B", String::class.java),
        c = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("c") as String).password,
        d = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("d") as String).password,
        e = (getProperty("e") as String?)?.toInt(),
        km = ITApiFactory.getService<KeystoreService>(KeystoreService::class.java, null).keyManager,
        ds = InitialContext.doLookup<DataSource>("""osgi:service/javax.sql.DataSource/(osgi.jndi.service.name=${getProperty("ds") as String})"""),
        injected = getProperty("injected") as Boolean,
        )
    setProperty("_RESULT_", output)
    body = responseFactory.encodeToString(String.serializer(), output.body)
    setHeader("Content-Type", responseContentType)
    setProperty("FOO", output.foo)
    setHeader("CamelHttpResponseCode", output.fooHeader)
    setHeader("X-FOO", output.optionalHeader)
    for (header in output.headers) {
      setHeader(header.key, header.value)
    }
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.fooSuspend(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
      requestFactory = JsonFactory
    }
    else -> {
      setHeader("Accept-Post", "application/json")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val executor = Executors.newCachedThreadPool()
    val executorCoroutineDispatcher = executor.asCoroutineDispatcher()
    val output = try {
      val scope = CoroutineScope(executorCoroutineDispatcher)
      val deferred = scope.async {
        fooSuspend(body = requestFactory.decodeFromString(String.serializer(), getBody(String::class.java)),
            b = getHeader("B", String::class.java),
            c = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("c") as String).password,
            d = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("d") as String).password,
            e = getProperty("E") as Int,
            )
      }
      deferred.asCompletableFuture().get()
    } catch (suspendUserError: ExecutionException) {
      throw suspendUserError.cause!!
    } finally {
      executorCoroutineDispatcher.close()
    }
    setProperty("_RESULT_", output)
    body = responseFactory.encodeToString(String.serializer(), output.body)
    setHeader("Content-Type", responseContentType)
    setProperty("FOO", output.foo)
    setHeader("CamelHttpResponseCode", output.fooHeader)
    for (header in output.headers) {
      setHeader(header.key, header.value)
    }
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.serialized(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
      requestFactory = JsonFactory
    }
    else -> {
      setHeader("Accept-Post", "application/json")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val output = serialized(body = requestFactory.decodeFromString(B.serializer(), getBody(String::class.java)),
        b = getHeader("B", String::class.java),
        )
    setProperty("_RESULT_", output)
    body = responseFactory.encodeToString(B.serializer(), output.b)
    setHeader("Content-Type", responseContentType)
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.typed(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
      requestFactory = JsonFactory
    }
    else -> {
      setHeader("Accept-Post", "application/json")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val output = typed(body = requestFactory.decodeFromString(C.serializer(Int.serializer(), ).nullable, getBody(String::class.java)),
        b = getHeader("B", String::class.java),
        injected = getProperty("injected") as Boolean,
        )
    setProperty("_RESULT_", output)
    body = responseFactory.encodeToString(D.serializer(Int.serializer(), ), output.b)
    setHeader("Content-Type", responseContentType)
    setHeader("CamelHttpResponseCode", output.foo)
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.noError(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  val output = noError(c = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("c") as String).password,
      d = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("d") as String).password,
      e = (getProperty("e") as String?)?.toInt(),
      )
  setProperty("_RESULT_", output)
  body = responseFactory.encodeToString(String.serializer(), output.body)
  setHeader("Content-Type", responseContentType)
  setProperty("FOO", output.foo)
  setHeader("CamelHttpResponseCode", output.fooHeader)
  for (header in output.headers) {
    setHeader(header.key, header.value)
  }
  return this
}

public fun Message.raw(messageLog: MessageLog): Message {
  raw(rawMessage = this@raw,
      rawMessageLog = messageLog,
      rawNullableMessageLog = messageLog,
      )
  return this
}

public fun Message.rawSuspend(messageLog: MessageLog): Message {
  val executor = Executors.newCachedThreadPool()
  val executorCoroutineDispatcher = executor.asCoroutineDispatcher()
  try {
    val scope = CoroutineScope(executorCoroutineDispatcher)
    val deferred = scope.async {
      rawSuspend(rawMessage = this@rawSuspend,
          rawMessageLog = messageLog,
          )
    }
    deferred.asCompletableFuture().get()
  } catch (suspendUserError: ExecutionException) {
    throw suspendUserError.cause!!
  } finally {
    executorCoroutineDispatcher.close()
  }
  return this
}

public fun Message.noOutputs(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  try {
    noOutputs(bb = getHeader("B", String::class.java),
        cc = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("cc") as String).password,
        dd = ITApiFactory.getService<SecureStoreService>(SecureStoreService::class.java, null).getUserCredential(getProperty("dd") as String).password,
        ee = (getProperty("ee") as String?)?.toInt(),
        )
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.setup(messageLog: MessageLog): Message {
  val output = setup()
  setProperty("_RESULT_", output)
  return this
}

public fun Message.twoPart1(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
      requestFactory = JsonFactory
    }
    else -> {
      setHeader("Accept-Post", "application/json")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val executor = Executors.newCachedThreadPool()
    val executorCoroutineDispatcher = executor.asCoroutineDispatcher()
    val output = try {
      val scope = CoroutineScope(executorCoroutineDispatcher)
      val deferred = scope.async {
        twoPart1(body = requestFactory.decodeFromString(FooInput.serializer(), getBody(String::class.java)),
            b = getHeader("B", String::class.java),
            injected = getProperty("injected") as Boolean,
            )
      }
      deferred.asCompletableFuture().get()
    } catch (suspendUserError: ExecutionException) {
      throw suspendUserError.cause!!
    } finally {
      executorCoroutineDispatcher.close()
    }
    setProperty("_RESULT_", output)
    body = output.body
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.twoPart2(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
      "application/json" if (parameters["charset"] == null || parameters["charset"] == "utf-8") -> {
        responseFactory = JsonFactory
        responseContentType = "application/json; charset=utf-8"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  try {
    val executor = Executors.newCachedThreadPool()
    val executorCoroutineDispatcher = executor.asCoroutineDispatcher()
    val output = try {
      val scope = CoroutineScope(executorCoroutineDispatcher)
      val deferred = scope.async {
        twoPart2(body = body as FooOutput2,
            b = getHeader("B", String::class.java),
            )
      }
      deferred.asCompletableFuture().get()
    } catch (suspendUserError: ExecutionException) {
      throw suspendUserError.cause!!
    } finally {
      executorCoroutineDispatcher.close()
    }
    setProperty("_RESULT_", output)
    body = responseFactory.encodeToString(String.serializer(), output.body)
    setHeader("Content-Type", responseContentType)
    setProperty("FOO", output.foo)
    setHeader("CamelHttpResponseCode", output.fooHeader)
    for (header in output.headers) {
      setHeader(header.key, header.value)
    }
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.javaStreams(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
      "application/octet-stream" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/octet-stream" -> {
      requestFactory = StreamFactory
    }
    else -> {
      setHeader("Accept-Post", "application/octet-stream")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val output = javaStreams(body = getBody(InputStream::class.java),
        b = getHeader("B", String::class.java),
        )
    setProperty("_RESULT_", output)
    body = output.body
    setHeader("Content-Type", responseContentType)
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.binaryRedirect(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
      "application/octet-stream" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  try {
    val output = binaryRedirect()
    setProperty("_RESULT_", output)
    body = null
    setHeader("Content-Type", responseContentType)
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.kotlinxIO(messageLog: MessageLog): Message {
  val acceptHeader: List<Pair<String, Map<String, String>>> = getHeader("Accept", String::class.java)?.split(",")?.map {
    val split = it.trim().split(";")
    val parameters = split.drop(1).associate {
      val (key, value) = it.trim().split("=")
      key to value
    }
    split[0] to parameters
  }?.sortedBy { (_, parameters) ->
    parameters["q"]?.toDouble() ?: 1.0
  } ?: listOf("*/*" to emptyMap())
  var responseFactory: StringFormat? = null
  var responseContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
      "application/octet-stream" -> {
        responseFactory = StreamFactory
        responseContentType = "application/octet-stream"
        break
      }
    }
  }
  if (responseFactory == null || responseContentType == null) {
    setHeader("CamelHttpResponseCode", 406)
    body = null
    return this
  }
  var errorFactory: StringFormat? = null
  var errorContentType: String? = null
  for ((contentType, parameters) in acceptHeader) {
    when (contentType) {
      "*/*" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
      "application/json" -> {
        errorFactory = Fault.ErrorJsonFactory
        errorContentType = "application/json"
        break
      }
    }
  }
  if (errorFactory == null || errorContentType == null) {
    errorFactory = Fault.ErrorJsonFactory
    errorContentType = "application/json"
  }
  val (contentType, parameters) = getHeader("Content-Type", String::class.java)?.split(";")?.let {
    val contentType = it[0]
    val parameters = it.drop(1).associate {
      val (key, value) = it.split("=")
      key to value
    }
    contentType to parameters
  } ?: (null to emptyMap())
  val requestFactory: StringFormat
  when (contentType) {
    "application/octet-stream" -> {
      requestFactory = StreamFactory
    }
    else -> {
      setHeader("Accept-Post", "application/octet-stream")
      setHeader("CamelHttpResponseCode", 415)
      body = null
      return this
    }
  }
  try {
    val output = kotlinxIO(body = getBody(InputStream::class.java).asSource().buffered(),
        b = getHeader("B", String::class.java),
        rawNullableMessageLog = messageLog,
        )
    setProperty("_RESULT_", output)
    body = output.body.asInputStream()
    setHeader("Content-Type", responseContentType)
  } catch (error: Fault) {
    body = errorFactory.encodeToString(Fault.serializer(), error.jsonError)
    messageLog.addAttachmentAsString("error", error.toString(), "text/plain")
    setHeader("Content-Type", errorContentType)
    setHeader("CamelHttpResponseCode", error.httpReturnCode)
  }
  return this
}

public fun Message.injectedBoolean(messageLog: MessageLog): Message {
  val output = injectedBoolean()
  setProperty("_RESULT_", output)
  return this
}

public fun Message.nullableReturn(messageLog: MessageLog): Message {
  val output = nullableReturn() ?: return this
  setProperty("_RESULT_", output)
  return this
}
