package eu.europa.ec.eudi.pidissuer.adapter.out.pid

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

interface KeycloakClient {
    /**
     * Fetches the details of a user.
     *
     * @param username The username of the user the details of whose to fetch
     */
    suspend fun getUserByUsername(username: String): UserRepresentation?
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
