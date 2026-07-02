package app.softwork.cikraft.ksp

import app.softwork.serviceloader.ServiceLoader
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*

@KspExperimental
@ServiceLoader(SymbolProcessorProvider::class)
public class SapCIGroovyEntrypointProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SapCIGroovyEntrypointPlugin =
        SapCIGroovyEntrypointPlugin(environment.codeGenerator)
}
