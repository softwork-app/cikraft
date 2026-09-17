package app.softwork.cikraft.gradle

import apiProxies
import app.softwork.cikraft.generator.generateOpenAPIProxyTransformer
import app.softwork.cikraft.proxy.ApiProxyBuilder
import app.softwork.cikraft.proxy.ApiProxyTransport
import app.softwork.cikraft.proxy.builder.ApiProxiesBuilder
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import app.softwork.cikraft.proxy.apiProxy as apiProxyOrigin

public abstract class GenerateOpenAPIProxyTransformerWorker :
    WorkAction<GenerateOpenAPIProxyTransformerWorker.Parameters> {
    public interface Parameters : WorkParameters {
        public val httpSuffix: Property<String>
        public val outputDirectory: DirectoryProperty
    }

    override fun execute() {
        val all = mutableListOf<ApiProxyTransport>()

        val apiProxyBuilder = object : ApiProxiesBuilder {
            override fun apiProxy(
                name: String,
                title: String,
                description: String?,
                builder: ApiProxyBuilder.() -> Unit,
            ) {
                val created = apiProxyOrigin(
                    name = name,
                    title = title,
                    description = description,
                    builder = builder,
                )
                all.add(created)
            }
        }
        apiProxyBuilder.apiProxies(parameters.httpSuffix.get())

        generateOpenAPIProxyTransformer(
            all,
        ).writeTo(parameters.outputDirectory.asFile.get())
    }
}
