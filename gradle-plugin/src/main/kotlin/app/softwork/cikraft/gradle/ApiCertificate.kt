package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface ApiCertificate : Named {
    val p12File: RegularFileProperty

    val storeName: Property<String>

    val description: Property<String>
}
