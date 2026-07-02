import kotlin.String

public enum class Stage(
  public val httpServer: String,
  public val apiHttpServer: String?,
) {
  /**
   * [Web](https://dev.home)
   */
  Dev(httpServer = "https://dev/", apiHttpServer = "https://dev.api/"),
  /**
   * Prod doc
   *
   * [Web](https://prd.home)
   */
  Prd(httpServer = "https://prd/", apiHttpServer = null),
  ;
}
