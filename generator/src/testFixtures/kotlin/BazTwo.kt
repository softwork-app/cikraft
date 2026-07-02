@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

import app.softwork.cikraft.integrationflow.EndpointBuilder
import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig
import app.softwork.cikraft.integrationflow.builder.IntegrationFlowBuilder
import kotlin.Any
import kotlin.String
import kotlin.Unit
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.listOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf

public data object BazTwo : CreatedFlowConfig, dummyWithOutput, twoPart1, dummy, twoPart2, setup {
  override val baseUrl: String = "foo"

  override val allowedHeaders: MutableSet<String> = mutableSetOf("Accept", "Content-Type")

  override val parameters: MutableMap<String, (Stage) -> Any> = mutableMapOf()

  override val suffix: String? = null
}

public fun IntegrationFlowBuilder.Baz_Two(builder: context(BazTwo) EndpointBuilder.() -> Unit) {
  createPackageAndFlow(
  packageId = "ComExampleKtorResources",
  packageName = "Com_Example_Ktor_Resources",
  packageDescription = "Test",
  integrationFlowId = "BazTwo",
  integrationFlowIdRaw = "BazTwo",
  integrationFlowName = "Baz_Two",
  integrationFlowNameRaw = "Baz_Two",
  integrationFlowDescription = "Foo Two API",
  integrationFlowSource = listOf(),
  integrationFlowTarget = listOf(),
  config = BazTwo,
  ) {
    builder(BazTwo, this)
  }
}
