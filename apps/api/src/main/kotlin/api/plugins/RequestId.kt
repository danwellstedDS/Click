package api.plugins

import io.ktor.server.application.*
import io.ktor.util.*
import java.util.UUID

val RequestIdKey = AttributeKey<String>("RequestId")

val RequestIdPlugin = createApplicationPlugin("RequestIdPlugin") {
    onCall { call ->
        val requestId = UUID.randomUUID().toString()
        call.attributes.put(RequestIdKey, requestId)
        call.response.headers.append("X-Request-Id", requestId)
    }
}
