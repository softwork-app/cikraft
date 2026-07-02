package app.softwork.cikraft.integrationflow

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.*
import nl.adaptivity.xmlutil.*
import nl.adaptivity.xmlutil.core.*
import nl.adaptivity.xmlutil.serialization.*
import java.io.*
import java.util.*
import java.util.Properties
import java.util.jar.*
import java.util.jar.Attributes.*
import java.util.zip.*
import kotlin.collections.Collection
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyList
import kotlin.collections.emptyMap
import kotlin.collections.joinToString
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.set

public data class CreateArtifact(
    val libs: Iterable<File> = emptyList(),
    val scripts: Iterable<File> = emptyList(),
    val name: String,
    val version: String,
    val description: String?,
    val source: List<String> = emptyList(),
    val target: List<String> = emptyList(),
    val parameters: Map<String, String> = emptyMap(),
    val integrationFlow: Definitions,
) {
    public fun toBase64(): String {
        val inMemoryZipFile = ByteArrayOutputStream()

        inMemoryZipFile.use {
            writeAsZip(it)
        }

        return Base64.getEncoder().encodeToString(inMemoryZipFile.toByteArray())
    }

    public fun writeAsZip(
        outputStream: OutputStream,
    ) {
        ZipOutputStream(outputStream).use {
            for (lib in libs) {
                it.entry("src/main/resources/lib/${lib.name}") {
                    lib.inputStream().copyTo(it)
                }
            }
            for (script in scripts) {
                it.entry("src/main/resources/script/${script.name}") {
                    script.inputStream().copyTo(it)
                }
            }

            it.entry("META-INF/MANIFEST.MF") { createManifestMF(name, version, it) }
            it.entry(".project", createProject(name))
            it.entry(
                "metainfo.prop",
                createMetainfo(description, source, target),
            )
            it.entry("src/main/resources/parameters.prop", createParametersProp(parameters))
            it.entry("src/main/resources/parameters.propdef", createParametersPropDef(parameters.keys))

            it.entry(
                "src/main/resources/scenarioflows/integrationflow/$name.iflw",
                definitionsXML.encodeToString(Definitions.serializer(), integrationFlow) + "\n",
            )
        }
    }

    public companion object {
        public val definitionsXML: XML = XML.v1 {
            xmlVersion = XmlVersion.XML10
            xmlDeclMode = XmlDeclMode.Charset
            isCollectingNSAttributes = true
            repairNamespaces = false
        }

        public fun fromBase64(content: String): CreateArtifact = ByteArrayInputStream(
            Base64.getDecoder().decode(content),
        ).use {
            fromZip(it)
        }

        public fun fromZip(inputStream: InputStream): CreateArtifact = ZipInputStream(inputStream).use {
            var manifest: Manifest? = null
            var description: String? = null
            var source: List<String>? = null
            var target: List<String>? = null
            var parameters: Map<String, String>? = null
            var integrationFlow: Definitions? = null

            while (true) {
                val nextEntry: ZipEntry = it.nextEntry ?: break
                val name = nextEntry.name
                when {
                    name == "META-INF/MANIFEST.MF" -> {
                        manifest = Manifest().apply { read(it) }
                    }

                    name == ".project" -> continue

                    name == "metainfo.prop" -> {
                        val s = Properties().apply { load(it) }
                        description = s["description"] as String?
                        source = (s["source"] as String?)?.split(",") ?: emptyList()
                        target = (s["target"] as String?)?.split(",") ?: emptyList()
                    }

                    name == "src/main/resources/parameters.prop" -> {
                        parameters = StringProperties.decodeMapFromString(it.bufferedReader().readText())
                    }

                    name == "src/main/resources/parameters.propdef" -> continue

                    name.startsWith("src/main/resources/scenarioflows/integrationflow/") -> {
                        val reader = KtXmlReader(it)
                        integrationFlow = definitionsXML.decodeFromReader(Definitions.serializer(), reader)
                    }

                    else -> error("Unexpected entry $name")
                }
            }
            requireNotNull(manifest)
            requireNotNull(integrationFlow)

            CreateArtifact(
                libs = emptyList(),
                scripts = emptyList(),
                name = manifest.mainAttributes[Name("Bundle-Name")]!!.toString(),
                version = manifest.mainAttributes[Name("Bundle-Version")]!!.toString(),
                description = description,
                source = source ?: emptyList(),
                target = target ?: emptyList(),
                parameters = parameters ?: emptyMap(),
                integrationFlow = integrationFlow,
            )
        }
    }
}

internal fun createParametersPropDef(parameters: Collection<String>): String = XML.v1 {
    xmlVersion = XmlVersion.XML10
    xmlDeclMode = XmlDeclMode.Charset
}.encodeToString(
    ParametersDef(
        parameters.map {
            ParametersDef.Parameter(name = it)
        },
    ),
) + "\n"

internal fun createProject(
    name: String,
): String = XML.v1 {
    xmlVersion = XmlVersion.XML10
    xmlDeclMode = XmlDeclMode.Charset
}.encodeToString(
    ProjectDescription(
        name = name.replace("_", ""),
        buildSpec = ProjectDescription.BuildSpec(
            buildCommand = ProjectDescription.BuildSpec.BuildCommand(
                "org.eclipse.jdt.core.javabuilder",
            ),
        ),
        natures = ProjectDescription.Natures(
            listOf(
                ProjectDescription.Nature("org.eclipse.jdt.core.javanature"),
                ProjectDescription.Nature("com.sap.ide.ifl.project.support.project.nature"),
                ProjectDescription.Nature("com.sap.ide.ifl.bsn"),
            ),
        ),
    ),
) + "\n"

internal fun createParametersProp(parameters: Map<String, Any>): String = StringProperties.encodeMapToString(parameters)

internal fun createManifestMF(
    name: String,
    version: String,
    outputStream: OutputStream,
) {
    val id = name.replace("_", "")
    val bytes = ByteArrayOutputStream()
    Manifest().apply {
        mainAttributes[Name.MANIFEST_VERSION] = "1.0"
        mainAttributes[Name("Bundle-ManifestVersion")] = "2"
        mainAttributes[Name("Bundle-Name")] = name
        mainAttributes[Name("Bundle-SymbolicName")] = "$id; singleton:=true"
        mainAttributes[Name("Bundle-Version")] = version
        mainAttributes[Name("SAP-BundleType")] = "IntegrationFlow"
        mainAttributes[Name("SAP-NodeType")] = "IFLMAP"
        mainAttributes[Name("SAP-RuntimeProfile")] = "iflmap"
        mainAttributes[Name("Import-Package")] =
            "com.sap.esb.application.services.cxf.interceptor,com.sap.esb.security,com.sap.it.op.agent.api,com.sap.it.op.agent.collector.camel,com.sap.it.op.agent.collector.cxf,com.sap.it.op.agent.mpl,javax.jms,javax.jws,javax.wsdl,javax.xml.bind.annotation,javax.xml.namespace,javax.xml.ws,org.apache.camel,org.apache.camel.builder,org.apache.camel.component.cxf,org.apache.camel.model,org.apache.camel.processor,org.apache.camel.processor.aggregate,org.apache.camel.spring.spi,org.apache.commons.logging,org.apache.cxf.binding,org.apache.cxf.binding.soap,org.apache.cxf.binding.soap.spring,org.apache.cxf.bus,org.apache.cxf.bus.resource,org.apache.cxf.bus.spring,org.apache.cxf.buslifecycle,org.apache.cxf.catalog,org.apache.cxf.configuration.jsse,org.apache.cxf.configuration.spring,org.apache.cxf.endpoint,org.apache.cxf.headers,org.apache.cxf.interceptor,org.apache.cxf.management.counters,org.apache.cxf.message,org.apache.cxf.phase,org.apache.cxf.resource,org.apache.cxf.service.factory,org.apache.cxf.service.model,org.apache.cxf.transport,org.apache.cxf.transport.common.gzip,org.apache.cxf.transport.http,org.apache.cxf.transport.http.policy,org.apache.cxf.workqueue,org.apache.cxf.ws.rm.persistence,org.apache.cxf.wsdl11,org.osgi.framework,org.slf4j,org.springframework.beans.factory.config,com.sap.esb.camel.security.cms,org.apache.camel.spi,com.sap.esb.webservice.audit.log,com.sap.esb.camel.endpoint.configurator.api,com.sap.esb.camel.jdbc.idempotency.reorg,javax.sql,org.apache.camel.processor.idempotent.jdbc,org.osgi.service.blueprint"
        mainAttributes[Name("Origin-Bundle-Name")] = name
        mainAttributes[Name("Origin-Bundle-SymbolicName")] = id

        write(bytes)
    }
    outputStream.writer().let {
        it.write(bytes.toString("UTF-8").replace("\r\n", "\n").dropLast(1))
        it.flush()
    }
}

internal fun createMetainfo(
    description: String?,
    source: List<String> = emptyList(),
    target: List<String> = emptyList(),
) = StringProperties.encodeToString(
    Metainfo(
        description,
        source.takeUnless { it.isEmpty() }?.joinToString(","),
        target.takeUnless { it.isEmpty() }?.joinToString(","),
    ),
)

@Serializable
private data class Metainfo(val description: String?, val source: String?, val target: String?)

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

private fun ZipOutputStream.entry(name: String, content: (ZipOutputStream) -> Unit) {
    putNextEntry(
        ZipEntry(name).apply {
            time = 0
        },
    )
    content(this)
    flush()
}
