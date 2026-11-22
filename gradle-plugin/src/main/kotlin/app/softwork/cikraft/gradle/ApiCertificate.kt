package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface ApiCertificate :
    Definition<ApiCertificateBuildModel>,
    Named {
    public val p12File: RegularFileProperty

    public val storeName: Property<String>

    public val description: Property<String>
}

public interface ApiCertificateBuildModel :
    BuildModel,
    Named {
    public val p12File: RegularFileProperty

    public val storeName: Property<String>

    public val description: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
internal abstract class DefaultApiCertificateBuildModel : ApiCertificateBuildModel {
    @get:JvmName("internalName")
    lateinit var name: String
    override fun getName(): String = name
}
