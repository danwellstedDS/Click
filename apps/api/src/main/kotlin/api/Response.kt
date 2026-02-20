package api

import api.plugins.RequestIdKey
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

data class ApiMeta(val requestId: String)
data class ApiSuccess<T>(val success: Boolean = true, val data: T, val meta: ApiMeta)
data class ApiError(val success: Boolean = false, val error: ErrorBody, val meta: ApiMeta)
data class ErrorBody(val code: String, val message: String)

suspend inline fun <reified T : Any> ApplicationCall.respondSuccess(
    data: T,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    val requestId = attributes.getOrNull(RequestIdKey) ?: "unknown"
    respond(status, ApiSuccess(data = data, meta = ApiMeta(requestId)))
}

suspend fun ApplicationCall.respondWithError(
    statusCode: Int,
    code: String,
    message: String
) {
    val requestId = attributes.getOrNull(RequestIdKey) ?: "unknown"
    respond(
        HttpStatusCode.fromValue(statusCode),
        ApiError(error = ErrorBody(code, message), meta = ApiMeta(requestId))
    )
}
