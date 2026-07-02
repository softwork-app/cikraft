package app.softwork.cikraft.proxy

import kotlinx.io.bytestring.ByteString
import nl.adaptivity.xmlutil.core.KtXmlReader
import nl.adaptivity.xmlutil.serialization.XML
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.collections.iterator
import kotlin.io.encoding.Base64

public data class ApiProxyTransport(
    public val apiProxy: ApiProxy,
    public val proxyEndpoints: List<ApiProxyEndPoint>,
    public val targetEndPoint: List<TargetEndPoint>,
    public val policies: Map<String, Policy>,
    public val resources: Map<String, ByteString>,
) {
    public val name: String get() = apiProxy.name

    public fun writeAsZip(outputStream: OutputStream) {
        ZipOutputStream(outputStream).use {
            it.entry(
                "APIProxy/${apiProxy.name}.xml",
                proxyXml.encodeToString(ApiProxy.serializer(), apiProxy),
            )
            for ((policyName, policy) in policies) {
                val policyXml = when (policy) {
                    is AssignMessage -> proxyXml.encodeToString(AssignMessage.serializer(), policy)
                    is Quota -> proxyXml.encodeToString(Quota.serializer(), policy)
                    is SpikeArrest -> proxyXml.encodeToString(SpikeArrest.serializer(), policy)
                    is RaiseFault -> proxyXml.encodeToString(RaiseFault.serializer(), policy)
                    is KeyValueMapOperations -> proxyXml.encodeToString(KeyValueMapOperations.serializer(), policy)
                    is BasicAuthentication -> proxyXml.encodeToString(BasicAuthentication.serializer(), policy)
                    is PopulateCache -> proxyXml.encodeToString(PopulateCache.serializer(), policy)
                    is LookupCache -> proxyXml.encodeToString(LookupCache.serializer(), policy)
                    is ServiceCallout -> proxyXml.encodeToString(ServiceCallout.serializer(), policy)
                    is VerifyJWT -> proxyXml.encodeToString(VerifyJWT.serializer(), policy)
                    is DecodeJWT -> proxyXml.encodeToString(DecodeJWT.serializer(), policy)
                    is ExtractVariables -> proxyXml.encodeToString(ExtractVariables.serializer(), policy)
                    is Javascript -> proxyXml.encodeToString(Javascript.serializer(), policy)
                }
                it.entry("APIProxy/Policy/$policyName.xml", policyXml)
            }
            for (proxyEndpoint in proxyEndpoints) {
                it.entry(
                    "APIProxy/APIProxyEndPoint/${proxyEndpoint.name}.xml",
                    proxyXml.encodeToString(ApiProxyEndPoint.serializer(), proxyEndpoint),
                )
            }
            for (targetEndpoint in targetEndPoint) {
                it.entry(
                    "APIProxy/APITargetEndPoint/${targetEndpoint.name}.xml",
                    proxyXml.encodeToString(TargetEndPoint.serializer(), targetEndpoint),
                )
            }
            for ((resourceName, resourceBytes) in resources) {
                it.entry("APIProxy/FileResource/$resourceName", resourceBytes.toByteArray())
            }
        }
    }

    public fun toBase64(): String {
        val inMemoryZipFile = ByteArrayOutputStream()
        inMemoryZipFile.use {
            writeAsZip(it)
        }

        return Base64.encode(inMemoryZipFile.toByteArray())
    }

    public companion object {
        public fun fromBase64(
            content: String,
        ): ApiProxyTransport = ByteArrayInputStream(Base64.decode(content)).use {
            fromZip(it)
        }

        public fun fromZip(inputStream: InputStream): ApiProxyTransport = ZipInputStream(inputStream).use {
            var apiProxy: ApiProxy? = null
            val proxyEndpoints = mutableListOf<ApiProxyEndPoint>()
            val targetEndpoints = mutableListOf<TargetEndPoint>()
            val policies = mutableMapOf<String, Policy>()
            val resources = mutableMapOf<String, ByteString>()

            while (true) {
                val entry = it.nextEntry ?: break
                val name: String = entry.name
                when {
                    name.startsWith("APIProxy/") && name.endsWith(".xml") && "/" !in name.removePrefix("APIProxy/") -> {
                        val reader = KtXmlReader(it)
                        apiProxy = proxyXml.decodeFromReader(ApiProxy.serializer(), reader)
                    }

                    name.startsWith("APIProxy/Policy/") -> {
                        val policyName = name.removePrefix("APIProxy/Policy/").removeSuffix(".xml")
                        val reader = KtXmlReader(it)
                        policies[policyName] = proxyXml.decodeFromReader(Policy.serializer(), reader)
                    }

                    name.startsWith("APIProxy/APIProxyEndPoint/") -> {
                        proxyEndpoints.add(proxyXml.decodeFromReader(ApiProxyEndPoint.serializer(), KtXmlReader(it)))
                    }

                    name.startsWith("APIProxy/APITargetEndPoint/") -> {
                        targetEndpoints.add(proxyXml.decodeFromReader(TargetEndPoint.serializer(), KtXmlReader(it)))
                    }

                    name.startsWith("APIProxy/FileResource/") -> {
                        resources[name.removePrefix("APIProxy/FileResource/")] = ByteString(it.readBytes())
                    }
                }
            }

            ApiProxyTransport(
                apiProxy = requireNotNull(apiProxy),
                proxyEndpoints = proxyEndpoints,
                targetEndPoint = targetEndpoints,
                policies = policies,
                resources = resources,
            )
        }
    }
}

internal val proxyXml: XML = XML.v1 {
    xmlVersion = XML10
    xmlDeclMode = Charset
    isCollectingNSAttributes = true
}

private fun ZipOutputStream.entry(name: String, content: String) {
    putNextEntry(
        ZipEntry(name).apply {
            time = 0
        },
    )
    bufferedWriter().apply {
        write(content)
        flush()
    }
}
private fun ZipOutputStream.entry(name: String, content: ByteArray) {
    putNextEntry(
        ZipEntry(name).apply {
            time = 0
        },
    )
    write(content)
    flush()
}
