package app.softwork.cikraft.gradle

import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LoggingConfig
import org.gradle.api.logging.Logger as GradleLogger

public fun LoggingConfig.setupGradleLogging(gradleLogger: GradleLogger) {
    level = if (gradleLogger.isDebugEnabled) {
        LogLevel.ALL
    } else {
        LogLevel.INFO
    }
    logger = object : Logger {
        override fun log(message: String) {
            if (gradleLogger.isDebugEnabled) {
                gradleLogger.debug(message)
            } else {
                gradleLogger.info(message)
            }
        }
    }
}
