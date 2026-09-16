package app.softwork.cikraft.generator

import app.softwork.cikraft.proxy.ApiProxyTransport
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

public fun generateOpenAPIProxyTransformer(
    proxies: List<ApiProxyTransport>,
): FileSpec = FileSpec.builder("", "APIProxyOpenAPITransformer").apply {

    val transformerClass = TypeSpec.classBuilder("APIProxyOpenAPITransformer").apply {
        addSuperinterface(ClassName("app.softwork.cikraft.core", "SAPOpenAPITransformer"))

        addFunction(
            FunSpec.builder("convert").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec.builder("openApi", ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi")).build())
                addParameter(ParameterSpec.builder("infrastructure", ClassName("app.softwork.cikraft.core", "OpenApiInfrastructure")).build())

                addStatement("val paths = openApi.paths")
                beginControlFlow("val apiPaths = %M", MemberName("kotlin.collections", "buildMap", isExtension = true))

                for (proxy in proxies) {
                    val apiPath = proxy.apiProxy.proxyEndPoints

                    addStatement("this[%S] = paths[%S]!!.let { it.copy(post = it.post!!.copy(security = listOf(), servers = listOf())) }",,)
                }
                endControlFlow()

                addStatement("return openApi.copy(servers = %M(), paths = apiPaths)", MemberName("kotlin.collections", "emptyList", isExtension = true))
            }.build()
        )


    }.build()
    addType(transformerClass)
}.build()
