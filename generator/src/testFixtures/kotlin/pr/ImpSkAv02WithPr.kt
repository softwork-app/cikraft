@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package pr

import Stage
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

public data object BazA : CreatedFlowConfig, foo {
  override val baseUrl: String = "foo"

  override val allowedHeaders: MutableSet<String> = mutableSetOf("Accept", "Content-Type")

  override val parameters: MutableMap<String, (Stage) -> Any> = mutableMapOf()

  override val suffix: String? = "PR42"
}

public fun IntegrationFlowBuilder.Baz_A(builder: context(BazA) EndpointBuilder.() -> Unit) {
  createPackageAndFlow(
  packageId = "ComExampleKtorResources",
  packageName = "Com_Example_Ktor_Resources",
  packageDescription = "FOO",
  integrationFlowId = "BazAPR42",
  integrationFlowIdRaw = "BazA",
  integrationFlowName = "Baz_A_PR42",
  integrationFlowNameRaw = "Baz_A",
  integrationFlowDescription = "Foo Bar API",
  integrationFlowSource = listOf("Foo"),
  integrationFlowTarget = listOf("Bar"),
  config = BazA,
  ) {
    builder(BazA, this)
  }
}
