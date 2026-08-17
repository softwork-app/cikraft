package app.softwork.cikraft.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ContextParameter
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

@ExperimentalKotlinPoetApi
public fun generateTypedGroovyStepBuilder(
    groovyScripts: List<String>,
): List<FileSpec> = groovyScripts.map {
    createGroovyEntryPointFile(it)
}

@ExperimentalKotlinPoetApi
private fun createGroovyEntryPointFile(groovyFileName: String) = FileSpec.builder("", groovyFileName).apply {
    addType(TypeSpec.interfaceBuilder(groovyFileName).addSuperinterface(CREATED_FLOW_CONFIG).build())

    val function = FunSpec.builder(groovyFileName)
    function.contextParameters(listOf(ContextParameter("config", ClassName("", groovyFileName))))

    function.receiver(STEPBUILDER)

    function.addStatement(
        "groovyScript(name = %S, file = %S)",
        groovyFileName,
        "$groovyFileName.groovy",
    )
    addFunction(function.build())
}.build()
