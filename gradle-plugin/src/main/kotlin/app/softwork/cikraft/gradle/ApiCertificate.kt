package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface ApiCertificate : Definition<ApiCertificateBuildModel>, Named {
    val p12File: RegularFileProperty

    val storeName: Property<String>

    val description: Property<String>
}

interface ApiCertificateBuildModel : BuildModel, Named {
    val p12File: RegularFileProperty

    val storeName: Property<String>

    val description: Property<String>
}

// https://github.com/gradle/gradle/issues/38207
abstract class DefaultApiCertificateBuildModel : ApiCertificateBuildModel {
    lateinit var name: String
    override fun getName(): String = name
}
