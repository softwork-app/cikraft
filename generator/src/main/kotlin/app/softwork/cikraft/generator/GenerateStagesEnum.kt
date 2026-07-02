package app.softwork.cikraft.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

public fun generateStagesEnum(
    stages: Map<String, EnumInput>,
): FileSpec {
    val file = FileSpec.builder("", "Stage")
    file.addType(stageEnum(stages))
    return file.build()
}

public class EnumInput(
    public val description: String?,
    public val httpServer: String,
    public val web: String,
    public val apiHttpServer: String?,
)

private fun stageEnum(stages: Map<String, EnumInput>): TypeSpec {
    val stageEnumBuilder = TypeSpec.enumBuilder("Stage")
    stageEnumBuilder.primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter("httpServer", STRING)
            .addParameter("apiHttpServer", STRING.copy(nullable = true))
            .build(),
    )
    stageEnumBuilder.addProperty(
        PropertySpec.builder("httpServer", STRING)
            .initializer("httpServer")
            .build(),
    )
    stageEnumBuilder.addProperty(
        PropertySpec.builder("apiHttpServer", STRING.copy(nullable = true))
            .initializer("apiHttpServer")
            .build(),
    )

    for ((stage, enumInput) in stages) {
        stageEnumBuilder.addEnumConstant(
            stage,
            typeSpec = TypeSpec.anonymousClassBuilder()
                .apply {
                    if (enumInput.description != null) {
                        addKdoc(enumInput.description + "\n\n")
                    }
                    addKdoc("[Web](${enumInput.web})")

                    addSuperclassConstructorParameter("httpServer = %S", enumInput.httpServer.removeSuffix("/") + "/")
                    addSuperclassConstructorParameter(
                        "apiHttpServer = %L",
                        if (enumInput.apiHttpServer == null) {
                            CodeBlock.of("null")
                        } else {
                            CodeBlock.of("%S", enumInput.apiHttpServer.removeSuffix("/") + "/")
                        },
                    )
                }
                .build(),
        )
    }

    return stageEnumBuilder.build()
}
