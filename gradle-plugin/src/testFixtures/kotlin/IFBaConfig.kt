import java.lang.System.getenv

public data object IFBaConfig {
    public val a: String
        get() = getenv("IF_BA_A") ?: "a"

    public val b: Int
        get() = getenv("IF_BA_B")?.toInt() ?: 0

    public val d: CharArray
        get() = getenv("IF_BA_D")!!.toCharArray()

    public val e: CharArray
        get() = getenv("IF_BA_E")!!.toCharArray()
}
