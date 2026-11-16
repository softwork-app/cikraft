package app.softwork.cikraft.generator

import app.softwork.cikraft.core.CreatedFlow
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

public fun generateKtorResources(
    createdFlow: CreatedFlow,
): FileSpec? = when (val sender = createdFlow.type) {
    is CreatedFlow.Type.Https -> {
        val file = FileSpec.builder(
            packageName = createdFlow.packageName.toPackageName(),
            fileName = createdFlow.rawId + "Resources",
        )

        val resource = TypeSpec.objectBuilder(createdFlow.rawId)
        resource.addAnnotation(
            AnnotationSpec.builder(ClassName("io.ktor.resources", "Resource"))
                .addMember("path = %S", sender.url.removePrefix("/"))
                .build(),
        )
        resource.addModifiers(KModifier.DATA)
        val description = createdFlow.description
        if (description != null) {
            resource.addKdoc(description)
        }

        file.addType(resource.build())
        file.build()
    }

    is CreatedFlow.Type.DataStore -> null

    is CreatedFlow.Type.Scheduler -> null

    null -> null
}

internal fun String.toPackageName() = replace('_', '.').lowercase()
