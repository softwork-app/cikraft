package ip.foo

import test
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.collections.Map

public fun IFBaFunction(
    input: String,
    a: String,
    b: Int,
    c: String,
    d: CharArray,
    e: CharArray,
    other: String,
): IFBaResult {
    val resultTest = test(input = input, a = a, b = b, c = c, d = d, e = e, other = other)
    return IFBaResult(
        foo = resultTest.foo,
        bar = resultTest.bar,
        baz = resultTest.baz,
        default = resultTest.default,
        headers = resultTest.headers,
    )
}

public data class IFBaResult(
    public val foo: String,
    public val default: String,
    public val baz: String,
    public val bar: String,
    public val headers: Map<String, String>,
)
