package app.softwork.cikraft.gradle

import org.gradle.api.artifacts.dsl.*

public interface IFlowDependencies : Dependencies {
    public val infrastructure: DependencyCollector
}
