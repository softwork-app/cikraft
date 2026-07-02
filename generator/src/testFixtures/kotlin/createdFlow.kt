import com.example.injectedBooleanScript
import com.example.nullableReturnScript
import com.example.sealedScript
import com.example.typedEnumScript
import com.example.typedListScript
import com.example.typedMapScript
import com.example.typedScript
import app.softwork.cikraft.core.CreatedFlow
import kotlin.time.Duration.Companion.minutes

val fooFlow = CreatedFlow(
    id = "BazA",
    rawId = "BazA",
    name = "Baz_A",
    rawName = "Baz_A",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "Foo",
        csrfProtection = true,
    ),
    description = "Foo Bar API",
    scripts = listOf(fooScript),
    injectedScripts = listOf(),
)

val noOutputsFlow = CreatedFlow(
    id = "BazNoOutputs",
    rawId = "BazNoOutputs",
    name = "Baz_NoOutputs",
    rawName = "Baz_NoOutputs",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/noOutputs",
        role = "Foo",
        csrfProtection = true,
    ),
    description = "Foo Bar API",
    scripts = listOf(noOutputsScript),
    injectedScripts = listOf(),
)

val scriptWithoutBodyAfterScriptWithBodyFlow = CreatedFlow(
    id = "BazNoOutputs",
    rawId = "BazNoOutputs",
    name = "Baz_NoOutputs",
    rawName = "Baz_NoOutputs",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/noOutputs",
        role = "Foo",
        csrfProtection = true,
    ),
    description = "Foo Bar API",
    scripts = listOf(twoPart1Script, dummyWithOutputScript),
    injectedScripts = listOf(),
)

val createdTypedFlow = CreatedFlow(
    id = "IFBa",
    rawId = "IFBa",
    name = "IF_Ba",
    rawName = "IF_Ba",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "SomeRole.send",
        csrfProtection = true,
    ),
    description = "Ba test",
    scripts = listOf(typedScript),
    injectedScripts = listOf(injectedBooleanScript),
)

val createdTypedEnumFlow = CreatedFlow(
    id = "IFBa",
    rawId = "IFBa",
    name = "IF_Ba",
    rawName = "IF_Ba",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "SomeRole.send",
        csrfProtection = true,
    ),
    description = "Ba test",
    scripts = listOf(typedEnumScript),
    injectedScripts = listOf(injectedBooleanScript),
)

val createdTypedListFlow = CreatedFlow(
    id = "IFBa",
    rawId = "IFBa",
    name = "IF_Ba",
    rawName = "IF_Ba",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "SomeRole.send",
        csrfProtection = true,
    ),
    description = "Ba test",
    scripts = listOf(typedListScript),
    injectedScripts = listOf(),
)

val createdTypedMapFlow = CreatedFlow(
    id = "IFBa",
    rawId = "IFBa",
    name = "IF_Ba",
    rawName = "IF_Ba",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "SomeRole.send",
        csrfProtection = true,
    ),
    description = "Ba test",
    scripts = listOf(typedMapScript),
    injectedScripts = listOf(),
)

val createdSealedFlow = CreatedFlow(
    id = "IFBa",
    rawId = "IFBa",
    name = "IF_Ba",
    rawName = "IF_Ba",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/baz",
        role = "SomeRole.send",
        csrfProtection = true,
    ),
    description = "Ba test",
    scripts = listOf(sealedScript),
    injectedScripts = listOf(),
)

val twoFlow = CreatedFlow(
    id = "BazTwo",
    rawId = "BazTwo",
    name = "Baz_Two",
    rawName = "Baz_Two",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "Test",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/two",
        role = "Foo",
        csrfProtection = true,
    ),
    description = "Foo Two API",
    scripts = listOf(twoPart1Script, dummyScript, dummyWithOutputScript, twoPart2Script),
    injectedScripts = listOf(setupScript),
)

val javaStreamFlow = CreatedFlow(
    id = "BazStream",
    rawId = "BazStream",
    name = "Baz_Stream",
    rawName = "Baz_Stream",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/stream",
        role = "Foo",
        csrfProtection = true,
    ),
    description = "Foo Stream API",
    scripts = listOf(javaStreamScript),
    injectedScripts = listOf(),
)

val kotlinxIoFlow = CreatedFlow(
    id = "BazKotlinxIO",
    rawId = "BazKotlinxIO",
    name = "Baz_KotlinxIO",
    rawName = "Baz_KotlinxIO",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo/bar/kotlinxio",
        role = "Foo",
        csrfProtection = false,
    ),
    description = "Foo KotlinxIO API",
    scripts = listOf(kotlinxIoScript),
    injectedScripts = listOf(),
)

val dataStoreFlow = CreatedFlow(
    id = "BazDataStore",
    rawId = "BazDataStore",
    name = "Baz_DataStore",
    rawName = "Baz_DataStore",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.DataStore(
        name = "FOO",
        pollDelay = 1.minutes,
    ),
    description = "Foo Bar DataStore API",
    scripts = listOf(dataStoreScript),
    injectedScripts = listOf(),
)

val nullableFlow = CreatedFlow(
    id = "BazDataStore",
    rawId = "BazDataStore",
    name = "Baz_DataStore",
    rawName = "Baz_DataStore",
    packageID = "ComExampleKtorResources",
    packageName = "Com_Example_Ktor_Resources",
    packageDescription = "",
    sender = CreatedFlow.Sender.Https(
        url = "/foo",
        role = "FOO",
        csrfProtection = false,
    ),
    description = "Foo Bar DataStore API",
    scripts = listOf(nullableReturnScript),
    injectedScripts = listOf(),
)
