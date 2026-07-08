# Module ksp-plugin

The [ksp](https://kotlinlang.org/docs/ksp-overview.html) plugin  extracts the meta information (Camel body, headers, properties and parameters) for each Kotlin function annotated with `@ScriptEntry`.
These information are used by the gradle-plugin to generate a GroovyScript that calls the Kotlin function with the required parameters.
