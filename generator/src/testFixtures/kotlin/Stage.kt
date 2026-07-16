import kotlin.String

public enum class Stage(
  public val httpServer: String,
) {
  /**
   * [Web](https://dev.home)
   */
  Dev(httpServer = "https://dev/"),
  /**
   * Prod doc
   *
   * [Web](https://prd.home)
   */
  Prd(httpServer = "https://prd/"),
  ;
}
