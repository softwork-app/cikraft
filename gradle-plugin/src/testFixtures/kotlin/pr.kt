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

public data object IFBa : CreatedFlowConfig, test {
  override val baseUrl: String = "foo/pr/42"

  override val allowedHeaders: MutableSet<String> = mutableSetOf("Accept", "Content-Type")

  override val parameters: MutableMap<String, (Stage) -> Any> = mutableMapOf()

  override val suffix: String? = "PR42"
}

public fun IntegrationFlowBuilder.IF_Ba(builder: context(IFBa) EndpointBuilder.() -> Unit) {
  createPackageAndFlow(
  packageId = "IPFoo",
  packageName = "IP_Foo",
  packageDescription = "Foo test",
  integrationFlowId = "IFBaPR42",
  integrationFlowIdRaw = "IFBa",
  integrationFlowName = "IF_Ba_PR42",
  integrationFlowNameRaw = "IF_Ba",
  integrationFlowDescription = "Ba test",
  integrationFlowSource = listOf(),
  integrationFlowTarget = listOf(),
  config = IFBa,
  ) {
    builder(IFBa, this)
  }
}
