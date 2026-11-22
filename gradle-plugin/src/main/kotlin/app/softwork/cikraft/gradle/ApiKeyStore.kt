package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface ApiKeyStore :
    Definition<ApiKeyStoreBuildModel>,
    Named

public interface ApiKeyStoreBuildModel :
    BuildModel,
    Named

// https://github.com/gradle/gradle/issues/38207
internal abstract class DefaultApiKeyStoreBuildModel : ApiKeyStoreBuildModel {
    @get:JvmName("internalName")
    lateinit var name: String
    override fun getName(): String = name
}
