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

public data object IFBaz : CreatedFlowConfig, test {
  override val baseUrl: String = "foo"

  override val allowedHeaders: MutableSet<String> = mutableSetOf("Accept", "Content-Type")

  override val parameters: MutableMap<String, (Stage) -> Any> = mutableMapOf()

  override val suffix: String? = null
}

public fun IntegrationFlowBuilder.IF_Baz(builder: context(IFBaz) EndpointBuilder.() -> Unit) {
  createPackageAndFlow(
  packageId = "IPFoo",
  packageName = "IP_Foo",
  packageDescription = "Foo test",
  integrationFlowId = "IFBaz",
  integrationFlowIdRaw = "IFBaz",
  integrationFlowName = "IF_Baz",
  integrationFlowNameRaw = "IF_Baz",
  integrationFlowDescription = "Baz test",
  integrationFlowSource = listOf(),
  integrationFlowTarget = listOf(),
  config = IFBaz,
  ) {
    builder(IFBaz, this)
  }
}
