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

public data object IFMultiple : CreatedFlowConfig, test, setup {
  override val baseUrl: String = "foo/pr/42"

  override val allowedHeaders: MutableSet<String> = mutableSetOf("Accept", "Content-Type")

  override val parameters: MutableMap<String, (Stage) -> Any> = mutableMapOf()

  override val suffix: String? = "PR42"
}

public fun IntegrationFlowBuilder.IF_Multiple(builder: context(IFMultiple) EndpointBuilder.() -> Unit) {
  createPackageAndFlow(
  packageId = "IPMultiple",
  packageName = "IP_Multiple",
  packageDescription = "Foo test",
  integrationFlowId = "IFMultiplePR42",
  integrationFlowIdRaw = "IFMultiple",
  integrationFlowName = "IF_Multiple_PR42",
  integrationFlowNameRaw = "IF_Multiple",
  integrationFlowDescription = "Multiple test",
  integrationFlowSource = listOf(),
  integrationFlowTarget = listOf(),
  config = IFMultiple,
  ) {
    builder(IFMultiple, this)
  }
}
