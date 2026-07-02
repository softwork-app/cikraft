package app.softwork.cikraft.ktor.server.runtime

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.parseAndSortContentTypeHeader
import io.ktor.server.request.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext

public fun Route.contentType(vararg contentTypes: ContentType, build: Route.() -> Unit): Route {
    val selector = MultiContentTypeHeaderRouteSelector(contentTypes.asList())
    return createChild(selector).apply(build)
}

private data class MultiContentTypeHeaderRouteSelector(
    val supportedContentTypes: List<ContentType>,
) : RouteSelector() {

    private val failedEvaluation = RouteSelectorEvaluation.Failure(
        RouteSelectorEvaluation.qualityFailedParameter,
        HttpStatusCode.UnsupportedMediaType,
    )

    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val headers = context.call.request.header(HttpHeaders.ContentType)
        val sortedRequestContentTypes = parseAndSortContentTypeHeader(headers)

        val matchedContentType = sortedRequestContentTypes.firstOrNull {
            val requestContentType = it.value
            supportedContentTypes.any { supportedContentType ->
                ContentType.parse(requestContentType).match(supportedContentType)
            }
        } ?: return failedEvaluation

        return RouteSelectorEvaluation.Success(matchedContentType.quality)
    }

    override fun toString(): String = "(contentTypes = $supportedContentTypes)"
}
