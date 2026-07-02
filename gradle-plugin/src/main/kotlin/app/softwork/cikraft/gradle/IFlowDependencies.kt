package app.softwork.cikraft.gradle

import org.gradle.api.artifacts.dsl.*

interface IFlowDependencies : Dependencies {
    val infrastructure: DependencyCollector
}
