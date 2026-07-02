import app.softwork.cikraft.core.Script
import app.softwork.cikraft.core.Script.Body
import app.softwork.cikraft.core.Script.DynamicHeaders
import app.softwork.cikraft.core.Script.Error
import app.softwork.cikraft.core.Script.Header
import app.softwork.cikraft.core.Script.None
import app.softwork.cikraft.core.Script.Parameter
import app.softwork.cikraft.core.Script.Password
import app.softwork.cikraft.core.Script.Property
import io.github.hfhbd.kfx.codegen.CodeGenTree
import io.github.hfhbd.kfx.codegen.CodeGenTree.ClassName
import io.github.hfhbd.kfx.codegen.CodeGenTree.Expression.ClassLiteral
import io.github.hfhbd.kfx.codegen.CodeGenTree.Member
import io.github.hfhbd.kfx.codegen.CodeGenTree.NormalClass
import io.github.hfhbd.kfx.codegen.CodeGenTree.Type.Builtin

val fault = Error(
    "com.example.core",
    "Fault",
    setOf(
        Body(
            "jsonError",
            NormalClass(
                "com.example.core",
                listOf("Fault"),
                members = listOf(
                    Member(name = "message", type = Builtin.STRING),
                    Member(
                        name = "input",
                        type = Builtin.STRING,
                        nullable = true,
                        documentation = "a message",
                    ),
                    Member(name = "statusCode", type = Builtin.INT, nullable = true),
                    Member(
                        name = "httpReturnCode",
                        type = Builtin.INT,
                        annotations = listOf(
                            CodeGenTree.Annotation(
                                "app.softwork.cikraft",
                                listOf("Header"),
                                mapOf("name" to CodeGenTree.Expression.StringLiteral("CamelHttpResponseCode")),
                            ),
                        ),
                    ),
                    Member(name = "sapMessageProcessingLogID", type = Builtin.STRING),
                ),
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to ClassLiteral(
                                CodeGenTree.ClassName(
                                    "kotlinx.serialization", listOf("KSerializer"),
                                ),
                            ),
                        ),
                    ),
                ),
                documentation = "Some Fault",
            ),
            sealedSubClasses = setOf(),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass(
                        "com.example.core",
                        listOf("Fault", "ErrorJsonFactory"),
                        isStatic = true,
                    ),
                    contentType = "application/json",
                    parameters = emptyMap(),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Header("CamelHttpResponseCode", "httpReturnCode", Builtin.INT, nullable = false, null),
    ),
    documentation = "if true",
)

public val fooScript = Script(
    name = "foo",
    jvmFunction = "com.example.FooKt.foo",
    isSuspend = false,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooInput"),
                members = listOf(
                    Member(
                        name = "s",
                        type = Builtin.STRING,
                        documentation = "asdf",
                    ),
                ),
                documentation = "Foo input sample",
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to ClassLiteral(
                                ClassName(
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
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        Password(propertyName = "c", documentation = "Foo", nullable = false),
        Password(
            propertyName = "d",
            documentation = null,
            nullable = false,
        ),
        Parameter(
            propertyName = "e",
            klass = Builtin.INT,
            nullable = true,
            documentation = null,
        ),
        None(
            propertyName = "km",
            documentation = null,
            klass = NormalClass("javax.net.ssl", listOf("KeyManager")),
            nullable = false,
            hasDefault = false,
        ),
        Property(
            propertyName = "ds",
            name = "ds",
            klass = NormalClass("javax.sql", listOf("DataSource")),
            nullable = true,
            documentation = null,
        ),
        None("injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
        None(propertyName = "ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = Builtin.STRING,
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
        Property(propertyName = "foo", name = "FOO", nullable = false, klass = Builtin.STRING, documentation = null),
        Header(
            name = "CamelHttpResponseCode",
            propertyName = "fooHeader",
            Builtin.INT,
            nullable = false,
            documentation = null,
        ),
        Header(
            name = "X-FOO",
            propertyName = "optionalHeader",
            Builtin.STRING,
            nullable = true,
            documentation = null,
        ),
        DynamicHeaders(propertyName = "headers", documentation = null),
    ),
    error = fault,
)

public val dataStoreScript = Script(
    name = "fooDataStore",
    jvmFunction = "com.example.FooKt.fooDataStore",
    isSuspend = false,
    inputs = listOf(
        Body(
            propertyName = "entries",
            klass = NormalClass(
                "app.softwork.cikraft",
                listOf("DataStoreMessages"),
                types = listOf(Builtin.STRING),
                members = listOf(),
                documentation = null,
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to ClassLiteral(
                                ClassName(
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
                    factoryKlass = NormalClass("com.example", listOf("XmlFactory"), isStatic = true),
                    contentType = "application/xml",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = false,
        ),
        Password(propertyName = "c", documentation = null, nullable = false),
        Password(
            propertyName = "d",
            documentation = null,
            nullable = false,
        ),
        Parameter(
            propertyName = "e",
            klass = Builtin.INT,
            nullable = true,
            documentation = null,
        ),
        None(
            propertyName = "km",
            documentation = null,
            klass = NormalClass("javax.net.ssl", listOf("KeyManager")),
            nullable = false,
            hasDefault = false,
        ),
        Property(
            propertyName = "ds",
            name = "ds",
            klass = NormalClass("javax.sql", listOf("DataSource")),
            nullable = true,
            documentation = null,
        ),
        None("injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
        None(propertyName = "ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = Builtin.STRING,
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
        Property(propertyName = "foo", name = "FOO", nullable = false, klass = Builtin.STRING, documentation = null),
        Header(
            name = "CamelHttpResponseCode",
            propertyName = "fooHeader",
            Builtin.INT,
            nullable = false,
            documentation = null,
        ),
        Header(
            name = "X-FOO",
            propertyName = "optionalHeader",
            Builtin.STRING,
            nullable = true,
            documentation = null,
        ),
        DynamicHeaders(propertyName = "headers", documentation = null),
    ),
    error = fault,
)

public val noOutputsScript = Script(
    name = "noOutputs",
    jvmFunction = "com.example.FooKt.noOutputs",
    isSuspend = false,
    inputs = listOf(
        Header(name = "B", propertyName = "bb", klass = Builtin.STRING, nullable = false, documentation = null),
        Password(propertyName = "cc", documentation = null, nullable = false),
        Password(
            propertyName = "dd",
            documentation = null,
            nullable = false,
        ),
        Parameter(
            propertyName = "ee",
            klass = Builtin.INT,
            nullable = true,
            documentation = null,
        ),
        None(propertyName = "ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "kotlin.Unit",
    outputs = setOf(),
    outputIsNullable = false,
    error = fault,
)

public val fooScriptNoError = Script(
    name = "noError",
    jvmFunction = "com.example.FooKt.noError",
    isSuspend = false,
    inputs = listOf(
        Password(propertyName = "c", documentation = null, nullable = false),
        Password(
            propertyName = "d",
            documentation = null,
            nullable = false,
        ),
        Parameter(
            propertyName = "e",
            klass = Builtin.INT,
            nullable = true,
            documentation = null,
        ),
        None(propertyName = "ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = Builtin.STRING,
            sealedSubClasses = setOf(),
            nullable = false,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
        ),
        Property(propertyName = "foo", name = "FOO", klass = Builtin.STRING, nullable = false, documentation = null),
        Header(
            name = "CamelHttpResponseCode",
            propertyName = "fooHeader",
            klass = Builtin.INT,
            nullable = false,
            documentation = null,
        ),
        DynamicHeaders(propertyName = "headers", documentation = null),
    ),
    error = null,
)

public val fooSuspendScript = Script(
    name = "fooSuspend",
    jvmFunction = "com.example.FooKt.fooSuspend",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = Builtin.STRING,
            nullable = false,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            sealedSubClasses = setOf(),
        ),
        Header("B", "b", Builtin.STRING, nullable = false, documentation = null),
        Password("c", documentation = null, nullable = false),
        Password("d", documentation = null, nullable = false),
        Property(propertyName = "e", name = "E", klass = Builtin.INT, nullable = false, documentation = null),
        None("ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            "body",
            Builtin.STRING,
            nullable = false,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            sealedSubClasses = setOf(),
        ),
        Property(propertyName = "foo", name = "FOO", klass = Builtin.STRING, nullable = false, documentation = null),
        Header("CamelHttpResponseCode", "fooHeader", Builtin.INT, nullable = false, documentation = null),
        DynamicHeaders("headers", documentation = null),
    ),
    error = fault,
)

public val fooWildcardScript = Script(
    name = "fooSuspend",
    jvmFunction = "com.example.FooKt.fooSuspend",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = Builtin.STRING,
            nullable = false,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            sealedSubClasses = setOf(),
        ),
        Header("B", "b", Builtin.STRING, nullable = false, documentation = null),
        Password("c", documentation = null, nullable = false),
        Password("d", documentation = null, nullable = false),
        Property(propertyName = "e", name = "E", klass = Builtin.INT, nullable = false, documentation = null),
        None(
            "ignored",
            NormalClass(
                packageName = "com.example",
                names = listOf("A"),
                types = listOf(CodeGenTree.Type.STAR),
            ),
            nullable = true,
            documentation = null,
            hasDefault = true,
        ),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            "body",
            Builtin.STRING,
            nullable = false,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            sealedSubClasses = setOf(),
        ),
        Property(propertyName = "foo", name = "FOO", klass = Builtin.STRING, nullable = false, documentation = null),
        Header("CamelHttpResponseCode", "fooHeader", Builtin.INT, nullable = false, documentation = null),
        DynamicHeaders("headers", documentation = null),
    ),
    error = fault,
)

public val rawScript = Script(
    name = "raw",
    jvmFunction = "com.example.FooKt.raw",
    isSuspend = false,
    inputs = listOf(
        None(
            "rawMessage",
            klass = NormalClass("com.sap.gateway.ip.core.customdev.util", listOf("Message")),
            nullable = false,
            documentation = null,
            hasDefault = true,
        ),
        None(
            "rawMessageLog",
            klass = NormalClass("com.sap.it.api.msglog", listOf("MessageLog")),
            nullable = false,
            documentation = null,
            hasDefault = false,
        ),
        None(
            "rawNullableMessageLog",
            klass = NormalClass("com.sap.it.api.msglog", listOf("MessageLog")),
            nullable = true,
            documentation = null,
            hasDefault = true,
        ),
    ),
    outputJvmName = "kotlin.Unit",
    outputIsNullable = false,
    outputs = emptySet(),
    error = null,
)

public val rawSuspendScript = Script(
    name = "rawSuspend",
    jvmFunction = "com.example.FooKt.rawSuspend",
    isSuspend = true,
    inputs = listOf(
        None(
            propertyName = "rawMessage",
            klass = NormalClass("com.sap.gateway.ip.core.customdev.util", listOf("Message")),
            nullable = false,
            documentation = null,
            hasDefault = false,
        ),
        None(
            propertyName = "rawMessageLog",
            klass = NormalClass("com.sap.it.api.msglog", listOf("MessageLog")),
            nullable = false,
            documentation = null,
            hasDefault = false,
        ),
    ),
    outputJvmName = "kotlin.Unit",
    outputIsNullable = false,
    outputs = emptySet(),
    error = null,
)

public val setupScript = Script(
    name = "setup",
    jvmFunction = "com.example.FooKt.setup",
    isSuspend = false,
    inputs = listOf(),
    outputJvmName = "kotlin.Boolean",
    outputIsNullable = false,
    outputs = setOf(),
    error = null,
)

public val twoPart1Script = Script(
    name = "twoPart1",
    jvmFunction = "com.example.FooKt.twoPart1",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooInput"),
                members = listOf(
                    Member(
                        name = "s",
                        type = Builtin.STRING,
                        documentation = "asdf",
                    ),
                ),
                documentation = "Foo input sample",
                annotations = listOf(
                    CodeGenTree.Annotation(
                        packageName = "kotlinx.serialization",
                        names = listOf("Serializable"),
                        values = mapOf(
                            "with" to ClassLiteral(
                                ClassName(
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
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        None(propertyName = "ignored", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
        None(propertyName = "injected", Builtin.BOOLEAN, nullable = false, documentation = null, hasDefault = false),
    ),
    outputJvmName = "com.example.TwoPart1Output",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooOutput2"),
            ),
            contentNegotiations = emptyList(),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
    ),
    error = fault,
)

public val dummyScript = Script(
    name = "dummy",
    jvmFunction = "com.example.FooKt.dummy",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooOutput2"),
            ),
            contentNegotiations = emptyList(),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        None(propertyName = "ignored2", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "kotlin.Unit",
    outputIsNullable = false,
    outputs = setOf(),
    error = fault,
)

public val dummyWithOutputScript = Script(
    name = "dummyWithOutput",
    jvmFunction = "com.example.FooKt.dummyWithOutput",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooOutput2"),
            ),
            contentNegotiations = emptyList(),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        None(propertyName = "ignored2", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.DummyOutput",
    outputIsNullable = false,
    outputs = setOf(
        Header(name = "D", propertyName = "d", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
    ),
    error = null,
)

public val twoPart2Script = Script(
    name = "twoPart2",
    jvmFunction = "com.example.FooKt.twoPart2",
    isSuspend = true,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass(
                "com.example",
                listOf("FooOutput2"),
            ),
            contentNegotiations = emptyList(),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        None(propertyName = "ignored2", Builtin.STRING, nullable = true, documentation = null, hasDefault = true),
    ),
    outputJvmName = "com.example.FooOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            Builtin.STRING,
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("JsonFactory"), isStatic = true),
                    contentType = "application/json",
                    parameters = mapOf("charset" to "utf-8"),
                ),
            ),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Property(propertyName = "foo", name = "FOO", nullable = false, klass = Builtin.STRING, documentation = null),
        Header(
            name = "CamelHttpResponseCode",
            propertyName = "fooHeader",
            Builtin.INT,
            nullable = false,
            documentation = null,
        ),
        DynamicHeaders(propertyName = "headers", documentation = null),
    ),
    error = fault,
)

public val javaStreamScript = Script(
    name = "javaStreams",
    jvmFunction = "com.example.FooKt.javaStreams",
    isSuspend = false,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass("java.io", listOf("InputStream")),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("StreamFactory"), isStatic = true),
                    contentType = "application/octet-stream",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
    ),
    outputJvmName = "com.example.StreamOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = NormalClass("java.io", listOf("InputStream")),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("StreamFactory"), isStatic = true),
                    contentType = "application/octet-stream",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
    ),
    error = fault,
)

public val binaryRedirectScript = Script(
    name = "binaryRedirect",
    jvmFunction = "com.example.FooKt.binaryRedirect",
    isSuspend = false,
    outputJvmName = "com.example.StreamOutput",
    outputIsNullable = false,
    inputs = listOf(),
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = NormalClass("kotlin", listOf("Nothing")),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("StreamFactory"), isStatic = true),
                    contentType = "application/octet-stream",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = true,
            sealedSubClasses = setOf(),
        ),
    ),
    error = fault,
)

public val kotlinxIoScript = Script(
    name = "kotlinxIO",
    jvmFunction = "com.example.FooKt.kotlinxIO",
    isSuspend = false,
    inputs = listOf(
        Body(
            propertyName = "body",
            klass = NormalClass("kotlinx.io", listOf("Source")),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("StreamFactory"), isStatic = true),
                    contentType = "application/octet-stream",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
        Header(name = "B", propertyName = "b", klass = Builtin.STRING, nullable = false, documentation = "some Header"),
        None(
            "rawNullableMessageLog",
            klass = NormalClass("com.sap.it.api.msglog", listOf("MessageLog")),
            nullable = true,
            documentation = null,
            hasDefault = true,
        ),
    ),
    outputJvmName = "com.example.SourceOutput",
    outputIsNullable = false,
    outputs = setOf(
        Body(
            propertyName = "body",
            klass = NormalClass("kotlinx.io", listOf("Source")),
            contentNegotiations = listOf(
                Body.ContentNegotiation(
                    factoryKlass = NormalClass("com.example", listOf("StreamFactory"), isStatic = true),
                    contentType = "application/octet-stream",
                    parameters = mapOf(),
                ),
            ),
            documentation = null,
            nullable = false,
            sealedSubClasses = setOf(),
        ),
    ),
    error = fault,
)
