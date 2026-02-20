package api

import domain.AuthClaims
import io.ktor.server.auth.*

data class AuthPrincipal(val claims: AuthClaims) : Principal
