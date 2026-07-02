package app.softwork.cikraft.generator

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.core.Script
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.hfhbd.kfx.kotlin.toPoetType

public fun generateKtorClientSetup(
    createdFlow: CreatedFlow,
): FileSpec? = when (val sender = createdFlow.sender) {
    is CreatedFlow.Sender.Https -> {
        val file = FileSpec.builder(
            packageName = createdFlow.packageName.toPackageName(),
            fileName = createdFlow.rawId + "ClientSetup",
        )

        val funcSpec = FunSpec.builder("setup${createdFlow.rawId}Client").apply {
            val t = TypeVariableName("T", ClassName("io.ktor.client.engine", "HttpClientEngineConfig"))

            receiver(ClassName("io.ktor.client", "HttpClientConfig").parameterizedBy(t))
            addTypeVariable(t)

            installResources()
            if (sender.csrfProtection) {
                installCookies()
            }
            installContentNegotiation(createdFlow)
        }.build()

        file.addFunction(funcSpec)
        file.build()
    }

    is CreatedFlow.Sender.DataStore -> null

    null -> null
}

private fun FunSpec.Builder.installResources() {
    addStatement(
        "install(%T)",
        ClassName("io.ktor.client.plugins.resources", "Resources"),
    )
}

private fun FunSpec.Builder.installCookies() {
    addStatement(
        "install(%T)",
        ClassName("io.ktor.client.plugins.cookies", "HttpCookies"),
    )
}

private fun FunSpec.Builder.installContentNegotiation(
    createdFlow: CreatedFlow,
) {
    beginControlFlow(
        "install(%M)",
        MemberName("io.ktor.client.plugins.contentnegotiation", "ContentNegotiation"),
    )

    val first = createdFlow.scripts.firstOrNull {
        it.bodyInput != null
    }
    val last = createdFlow.scripts.lastOrNull {
        it.bodyOutput != null
    }

    val bodyInput = first?.bodyInput
    if (bodyInput != null) {
        serialization(bodyInput)
    }

    val bodyOutput = last?.bodyOutput
    if (bodyOutput != null) {
        serialization(bodyOutput)
    }

    val errorBody = createdFlow.scripts.lastOrNull { it.error != null }?.error?.bodyOutput
    if (errorBody != null) {
        serialization(errorBody)
    }

    endControlFlow()
}

private fun FunSpec.Builder.serialization(body: Script.Body) {
    for (contentNegotiation in body.contentNegotiations) {
        addStatement(
            "%M(%L, %T%L)",
            MemberName("io.ktor.serialization.kotlinx", "serialization"),
            contentNegotiation.contentTypeKtor(includeParams = true),
            contentNegotiation.factoryKlass.toPoetType(),
            CodeBlock.of(if (contentNegotiation.factoryKlass.isStatic) "" else "()"),
        )
    }
}
