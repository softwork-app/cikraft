package com.example

import app.softwork.cikraft.core.*
import app.softwork.cikraft.core.Script.*
import fault
import io.github.hfhbd.kfx.codegen.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.*
import io.github.hfhbd.kfx.codegen.CodeGenTree.Type.*

public val serializedScript = Script(
    name = "serialized",
    jvmFunction = "SerializedKt.serialized",
    isSuspend = false,
    inputs = listOf(
        Script.Body(
            propertyName = "body",
            klass = NormalClass(
                packageName = "",
                names = listOf("B"),
                members = listOf(
                    Member(
                        name = "x",
                        type = Builtin.INT,
                    ),
                ),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                    emptyList(),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = "some Body",
            nullable = false,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "SerializedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            NormalClass(
                "",
                listOf("B"),
                members = listOf(
                    Member(
                        name = "x",
                        type = Builtin.INT,
                    ),
                ),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = "some Output body",
            nullable = false,
        ),
    ),
    error = fault,
)

public val injectedBooleanScript = Script(
    name = "injectedBoolean",
    jvmFunction = "TypedKt.injectedBoolean",
    isSuspend = false,
    inputs = listOf(),
    outputs = setOf(),
    error = null,
    outputJvmName = "kotlin.Boolean",
    outputIsNullable = false,
)

public val nullableReturnScript = Script(
    name = "nullableReturn",
    jvmFunction = "NullableReturnKt.nullableReturn",
    isSuspend = false,
    inputs = listOf(),
    outputs = setOf(),
    error = null,
    outputJvmName = "kotlin.Boolean",
    outputIsNullable = true,
)

public val starScript = Script(
    name = "star",
    jvmFunction = "TypedKt.star",
    isSuspend = false,
    inputs = listOf(
        Script.Body(
            "body",
            nullable = true,
            klass = NormalClass(
                "",
                listOf("C"),
                members = listOf(Member("x", type = Builtin.INT)),
                types = listOf(Builtin.INT),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "StarOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            NormalClass(
                "",
                listOf("D"),
                members = listOf(
                    Member("x", type = Builtin.INT),
                    Member(
                        "nested",
                        type = NormalClass(
                            packageName = "",
                            names = listOf("E"),
                            members = listOf(Member("f", type = Builtin.STRING)),
                            annotations = listOf(
                                CodeGenTree.Annotation(
                                    packageName = "kotlinx.serialization",
                                    names = listOf("Serializable"),
                                    values = mapOf(
                                        "with" to CodeGenTree.Expression.ClassLiteral(
                                            CodeGenTree.ClassName(
                                                "kotlinx.serialization", listOf("KSerializer"),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                types = listOf(STAR),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.INT, nullable = false, documentation = null),
    ),
    error = fault,
)

public val typedScript = Script(
    name = "typed",
    jvmFunction = "TypedKt.typed",
    isSuspend = false,
    inputs = listOf(
        Script.Body(
            "body",
            nullable = true,
            klass = NormalClass(
                "",
                listOf("C"),
                members = listOf(Member("x", type = Builtin.INT)),
                types = listOf(Builtin.INT),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "TypedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            NormalClass(
                "",
                listOf("D"),
                members = listOf(
                    Member("x", type = Builtin.INT),
                    Member(
                        "nested",
                        type = NormalClass(
                            packageName = "",
                            names = listOf("E"),
                            members = listOf(Member("f", type = Builtin.STRING)),
                            annotations = listOf(
                                CodeGenTree.Annotation(
                                    packageName = "kotlinx.serialization",
                                    names = listOf("Serializable"),
                                    values = mapOf(
                                        "with" to CodeGenTree.Expression.ClassLiteral(
                                            CodeGenTree.ClassName(
                                                "kotlinx.serialization", listOf("KSerializer"),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                types = listOf(Builtin.INT),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.INT, nullable = false, documentation = null),
    ),
    error = fault,
)

public val typedEnumScript = Script(
    name = "typed",
    jvmFunction = "TypedKt.typed",
    isSuspend = false,
    inputs = listOf(
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "TypedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            NormalClass(
                "",
                listOf("D"),
                members = listOf(
                    Member("x", type = Builtin.INT),
                    Member(
                        "nested",
                        type = NormalClass(
                            packageName = "",
                            names = listOf("E"),
                            members = listOf(Member("f", type = Builtin.STRING)),
                            annotations = listOf(
                                CodeGenTree.Annotation(
                                    packageName = "kotlinx.serialization",
                                    names = listOf("Serializable"),
                                    values = mapOf(
                                        "with" to CodeGenTree.Expression.ClassLiteral(
                                            CodeGenTree.ClassName(
                                                "kotlinx.serialization", listOf("KSerializer"),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    Member(
                        "e",
                        documentation = "Some Enum description at D",
                        type = CodeGenTree.Enum(
                            packageName = "",
                            names = listOf("ENUM"),
                            values = listOf(
                                CodeGenTree.Enum.Value(
                                    name = "FOO",
                                    annotations = listOf(
                                        CodeGenTree.Annotation(
                                            packageName = "kotlinx.serialization",
                                            names = listOf("SerialName"),
                                            values = mapOf(
                                                "value" to CodeGenTree.Expression.StringLiteral("foo"),
                                            ),
                                        ),
                                    ),
                                ),
                                CodeGenTree.Enum.Value(name = "Bar"),
                            ),
                        ),
                        annotations = listOf(
                            CodeGenTree.Annotation(
                                packageName = "kotlinx.serialization",
                                names = listOf("Serializable"),
                                values = mapOf(
                                    "with" to CodeGenTree.Expression.ClassLiteral(
                                        CodeGenTree.ClassName(
                                            "kotlinx.serialization", listOf("KSerializer"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                types = listOf(Builtin.INT),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to CodeGenTree.Expression.ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory")),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.INT, nullable = false, documentation = null),
    ),
    error = fault,
)

public val typedListScript = Script(
    name = "typed",
    jvmFunction = "TypedKt.typed",
    isSuspend = false,
    inputs = listOf(
        // body: List<D<Int>>, class D<F>(x: Int, y: List<F>, z: List<Int>)
        Script.Body(
            "body",
            Type.LIST(
                NormalClass(
                    packageName = "",
                    names = listOf("D"),
                    members = listOf(
                        Member(name = "x", type = Builtin.INT),
                        Member(
                            "y",
                            type = Type.LIST(Builtin.INT),
                        ),
                        Member(
                            "z",
                            type = Type.LIST(
                                NormalClass(
                                    "",
                                    listOf("I"),
                                    members = listOf(Member("ii", type = Builtin.STRING)),
                                ),
                            ),
                        ),
                    ),
                    types = listOf(Builtin.INT),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "TypedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            Type.LIST(
                NormalClass(
                    "",
                    listOf("C"),
                    members = listOf(Member("x", type = Builtin.INT)),
                    types = listOf(Builtin.INT),
                ),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.STRING, nullable = false, documentation = null),
    ),
    error = fault,
)

public val typedMapScript = Script(
    name = "typed",
    jvmFunction = "TypedKt.typed",
    isSuspend = false,
    inputs = listOf(
        // body: List<D<Int>>, class D<F>(x: Int, y: List<F>, z: List<Int>)
        Script.Body(
            "body",
            NormalClass(
                packageName = "",
                names = listOf("C"),
                members = listOf(Member("x", type = Builtin.INT)),
                types = listOf(Builtin.INT),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "TypedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "body",
            NormalClass(
                packageName = "",
                names = listOf("D"),
                members = listOf(
                    Member(name = "s", type = MAP(Builtin.STRING, Builtin.STRING)),
                    Member(
                        "t",
                        type = MAP(Builtin.STRING, Builtin.INT),
                    ),
                    Member(
                        "i",
                        type = MAP(Builtin.STRING, Builtin.INT),
                    ),
                    Member(
                        "o",
                        type = MAP(
                            Builtin.STRING,
                            NormalClass(
                                "kotlin",
                                listOf("Pair"),
                                members = listOf(
                                    Member("first", type = Builtin.STRING),
                                    Member(
                                        "second",
                                        type = NormalClass(
                                            packageName = "",
                                            names = listOf("E"),
                                            members = listOf(
                                                Member("f", type = Builtin.STRING),
                                            )
                                        )
                                    ),
                                ),
                                types = listOf(Builtin.STRING, NormalClass(
                                    packageName = "",
                                    names = listOf("E"),
                                    members = listOf(
                                        Member("f", type = Builtin.STRING),
                                    )
                                )),
                            ),
                        ),
                    ),
                ),
                types = listOf(Builtin.INT),
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.STRING, nullable = false, documentation = null),
    ),
    error = fault,
)

public val sealedScript = Script(
    name = "sealed",
    jvmFunction = "TypedKt.sealed",
    isSuspend = false,
    inputs = listOf(
        Script.Body(
            "body",
            Type.LIST(
                NormalClass(
                    packageName = "",
                    names = listOf("Base"),
                    members = listOf(
                        Member(name = "base", type = Builtin.STRING),
                    ),
                    types = listOf(),
                    isSealed = true,
                ),
            ),
            sealedSubClasses = setOf(
                SealedSubClass(
                    parent = ClassName("", listOf("Base"), emptyList()),
                    NormalClass(
                        packageName = "",
                        names = listOf("Sub"),
                        members = listOf(
                            Member("name", type = Builtin.STRING),
                            Member("base", type = Builtin.STRING),
                        ),
                    ),
                ),
                SealedSubClass(
                    parent = ClassName("", listOf("Base"), emptyList()),
                    NormalClass(
                        packageName = "",
                        names = listOf("SubSerialName"),
                        members = listOf(
                            Member("foo", type = Builtin.INT),
                            Member("base", type = Builtin.STRING),
                        ),
                        annotations = listOf(
                            CodeGenTree.Annotation(
                                packageName = "kotlinx.serialization",
                                names = listOf("SerialName"),
                                values = mapOf(
                                    "value" to CodeGenTree.Expression.StringLiteral("serial"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("B", "b", Builtin.STRING, nullable = false, documentation = "some Header"),
        Script.None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "SealedOutput",
    outputIsNullable = false,
    outputs = setOf(
        Script.Body(
            "b",
            Type.LIST(
                Builtin.INT,
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Script.Header("CamelHttpResponseCode", "foo", Builtin.STRING, nullable = false, documentation = null),
    ),
    error = fault,
)
