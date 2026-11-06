package app.softwork.cikraft.gradle

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class TransportLockBuildService : BuildService<TransportLockBuildService.Parameters> {
    interface Parameters : BuildServiceParameters {
        val packageId: Property<String>
        val apiSourceStage: Property<String>
    }
}
