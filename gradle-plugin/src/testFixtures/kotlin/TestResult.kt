fun test(
    input: String,
    a: String,
    b: Int?,
    c: String,
    d: CharArray,
    e: CharArray,
    other: String,
): TestResult = error("")

interface TestResult {
    val foo: String
    val bar: String
    val baz: String
    val default: String
    val headers: Map<String, String>
}
