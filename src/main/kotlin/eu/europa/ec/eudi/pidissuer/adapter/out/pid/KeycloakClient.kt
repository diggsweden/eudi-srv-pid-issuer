package eu.europa.ec.eudi.pidissuer.adapter.out.pid

import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import io.ktor.http.toURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

data class Credentials(val username: String, val password: String?) {
    init {
        require(username.isNotBlank()) { "username cannot be blank" }
    }
}

@JvmInline
value class Realm(val value: String) {
    init {
        require(value.isNotBlank()) { "realm cannot be blank" }
    }
}

data class AdministrationClient(
    val realm: Realm,
    val client: Credentials,
    val admin: Credentials,
)

class KeycloakClient(
    val webClient: WebClient,
    val keyCloak: Url,
    val administrationClient: AdministrationClient,
    val users: Realm
) {

    /**
     * Fetches the details of a user.
     *
     * @param username The username of the user the details of whose to fetch
     */
    suspend fun getUserByUsername(username: String): UserRepresentation? {
        val accessToken = getAdminAccessToken()
        val url = URLBuilder()
            .takeFrom(keyCloak)
            .appendPathSegments("admin", "realms", users.value, "users")
            .apply {
                parameters.append("username", username)
                parameters.append("exact", "true")
            }
            .build()

        val users = webClient.get()
            .uri(url.toURI())
            .accept(MediaType.APPLICATION_JSON)
            .headers {
                it[HttpHeaders.AUTHORIZATION] = accessToken.toAuthorizationHeader()
            }
            .retrieve()
            .awaitBody<List<UserRepresentation>>()

        return if (users.size != 1) {
            null
        } else {
            users.first()
        }
    }

    /**
     * Gets an Access Token for the Admin user, using OAuth2.0 Password Grant.
     */
    private suspend fun getAdminAccessToken(): AccessToken {
        val tokenEndpoint = URLBuilder()
            .takeFrom(keyCloak)
            .appendPathSegments("realms", administrationClient.realm.value, "protocol", "openid-connect", "token")
            .build()
        val response = webClient.post()
            .uri(tokenEndpoint.toURI())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromFormData(
                    LinkedMultiValueMap<String, String>()
                        .apply {
                            add("grant_type", "password")
                            add("client_id", administrationClient.client.username)
                            administrationClient.client.password?.let { add("client_secret", it) }
                            add("username", administrationClient.admin.username)
                            administrationClient.admin.password?.let { add("password", it) }
                        },
                ),
            )
            .retrieve()
            .awaitBody<String>()

        return withContext(Dispatchers.Default) {
            AccessToken.parse(JSONObjectUtils.parse(response))
        }
    }
}

@Serializable
@JsonIgnoreUnknownKeys
data class UserRepresentation(
    @Required val username: String,
    @Required val lastName: String,
    @Required val firstName: String,
    val attributes: Map<String, List<String>> = emptyMap(),
    val email: String? = null,
)
