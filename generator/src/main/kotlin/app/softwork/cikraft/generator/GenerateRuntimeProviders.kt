package app.softwork.cikraft.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING

public fun generateRuntimeProviders(
    providers: Map<String, String?>,
): FileSpec = FileSpec.builder("", "providers").apply {
    for ((provider, description) in providers) {
        addProperty(
            PropertySpec.builder(provider, STRING)
                .apply {
                    initializer("%S", description)
                    addModifiers(KModifier.CONST)
                    if (description != null) {
                        addKdoc(description)
                    }
                }
                .build(),
        )
    }
}.build()
