import test
import app.softwork.cikraft.integrationflow.StepBuilder
import Stage

val userRole = "SomeRole.send"

context(config: test)
public fun StepBuilder.testWithDefaults() {
    test(
        a = { when (it)  {
            Stage.Dev -> "a"
            Stage.Prd -> "b"
        }},
        b = { 42 },
        d = { "foo" },
        e = { "foo" },
    )
}
