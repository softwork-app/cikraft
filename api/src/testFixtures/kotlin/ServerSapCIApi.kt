import app.softwork.cikraft.api.BuildAndDeployStatus
import app.softwork.cikraft.api.CSRF_TOKEN
import app.softwork.cikraft.api.IntegrationFlow
import app.softwork.cikraft.api.IntegrationPackage
import app.softwork.cikraft.api.ResponseWrapper
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.getCSRFToken(
    action: suspend ApplicationCall.() -> String,
) {
    get("/") {
        val fetchToken = call.request.headers[CSRF_TOKEN]
        if (fetchToken != "Fetch") {
            call.respond(BadRequest)
        }
        val token = call.action()
        call.response.header(CSRF_TOKEN, token)
        call.respond(OK)
    }
}

fun Route.getIntegrationPackage(
    action: suspend ApplicationCall.(String) -> IntegrationPackage?
) {
    get("IntegrationPackages('{id}')") {
        val id = call.parameters["id"]
        requireNotNull(id)
        when (val response = call.action(id)) {
            null -> {
                call.respond(NotFound)
            }

            else -> {
                call.response.status(OK)
                call.respond(ResponseWrapper(response))
            }
        }
    }
}

fun Route.deleteIntegrationPackage(
    action: suspend ApplicationCall.(String) -> Unit?
) {
    delete("IntegrationPackages('{id}')") {
        val id = call.parameters["id"]
        requireNotNull(id)
        when (call.action(id)) {
            null -> {
                call.respond(NotFound)
            }

            else -> {
                call.respond(NoContent)
            }
        }
    }
}

fun Route.createIntegrationPackage(
    action: suspend ApplicationCall.(IntegrationPackage.New) -> IntegrationPackage?,
) {
    post("IntegrationPackages") {
        val body = call.receive<IntegrationPackage.New>()
        when (val response = call.action(body)) {
            null -> {
                call.respond(BadRequest)
            }

            else -> {
                call.response.status(OK)
                call.respond(ResponseWrapper(response))
            }
        }
    }
}

fun Route.createIntegrationFlow(
    action: suspend ApplicationCall.(IntegrationFlow.New) -> IntegrationFlow?,
) {
    post("IntegrationDesigntimeArtifacts") {
        val body = call.receive<IntegrationFlow.New>()
        when (val response = call.action(body)) {
            null -> {
                call.respond(BadRequest)
            }

            else -> {
                call.response.status(OK)
                call.respond(ResponseWrapper(response))
            }
        }
    }
}

fun Route.deployIntegrationFlow(
    action: suspend ApplicationCall.(String) -> String?,
) {
    post("DeployIntegrationDesigntimeArtifact") {
        val id = call.parameters["id"]
        requireNotNull(id)
        when (val response = call.action(id)) {
            null -> {
                call.respond(BadRequest)
            }

            else -> {
                call.response.status(Accepted)
                call.respond(response)
            }
        }
    }
}

fun Route.undeployIntegrationFlow(
    action: suspend ApplicationCall.(String) -> Unit?,
) {
    delete("IntegrationRuntimeArtifacts(Id='{id}')") {
        val id = call.parameters["id"]
        requireNotNull(id)
        when (val response = call.action(id)) {
            null -> {
                call.respond(NotFound)
            }

            else -> {
                call.respond(NoContent)
            }
        }
    }
}

fun Route.getBuildAndDeployStatus(
    action: suspend ApplicationCall.(String) -> BuildAndDeployStatus?,
) {
    get("BuildAndDeployStatus(TaskId='{taskId}')") {
        val taskId = call.parameters["taskId"]
        requireNotNull(taskId)
        when (val response = call.action(taskId)) {
            null -> {
                call.respond(BadRequest)
            }

            else -> {
                call.response.status(OK)
                call.respond(ResponseWrapper(response))
            }
        }
    }
}
