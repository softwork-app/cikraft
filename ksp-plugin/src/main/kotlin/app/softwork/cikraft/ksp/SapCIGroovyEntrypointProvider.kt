package app.softwork.cikraft.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import io.github.hfhbd.serviceloader.ServiceLoader

@KspExperimental
@ServiceLoader(SymbolProcessorProvider::class)
public class SapCIGroovyEntrypointProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SapCIGroovyEntrypointPlugin =
        SapCIGroovyEntrypointPlugin(environment.codeGenerator)
}
