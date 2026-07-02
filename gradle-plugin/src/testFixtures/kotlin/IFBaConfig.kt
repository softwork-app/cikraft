import app.softwork.cikraft.ktor.server.runtime.env

public data object IFBaConfig {
    public val a: String
        get() = env("IF_BA_A") ?: "a"

    public val b: Int
        get() = env("IF_BA_B")?.toInt() ?: 0

    public val d: CharArray
        get() = env("IF_BA_D")!!.toCharArray()

    public val e: CharArray
        get() = env("IF_BA_E")!!.toCharArray()
}
