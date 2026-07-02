package app.softwork.cikraft.kotlin

import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.resolve.annotations.*

internal val scriptEntryFq = FqName("app.softwork.cikraft.ScriptEntry")
internal val scriptEntryClass = ClassId.topLevel(scriptEntryFq)

internal val bodyFq = FqName("app.softwork.cikraft.Body")
internal val bodyClass = ClassId.topLevel(bodyFq)
internal val bodyWith = Name.identifier("app.softwork.cikraft.Body.with")

internal val contentTypeFq = FqName("app.softwork.cikraft.ContentType")
internal val contentTypeClass = ClassId.topLevel(contentTypeFq)

internal val propertyFq = FqName("app.softwork.cikraft.Property")
internal val propertyClass = ClassId.topLevel(propertyFq)

internal val headerFq = FqName("app.softwork.cikraft.Header")
internal val headerClass = ClassId.topLevel(headerFq)

internal val dynamicHeadersFq = FqName("app.softwork.cikraft.DynamicHeaders")
internal val dynamicHeadersClass = ClassId.topLevel(dynamicHeadersFq)

internal val passwordFq = FqName("app.softwork.cikraft.Password")
internal val passwordClass = ClassId.topLevel(passwordFq)

internal val throwsClass = ClassId.topLevel(JVM_THROWS_ANNOTATION_FQ_NAME)
