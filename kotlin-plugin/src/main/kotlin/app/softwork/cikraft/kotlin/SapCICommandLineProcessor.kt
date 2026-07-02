package app.softwork.cikraft.kotlin

import app.softwork.cikraft.kotlin.SapCIPluginRegistrar.Companion.PLUGIN_ID
import app.softwork.serviceloader.ServiceLoader
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@ServiceLoader(CommandLineProcessor::class)
public class SapCICommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: List<CliOption> = listOf()

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}
