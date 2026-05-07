package eu.europa.ec.eudi.pidissuer.adapter.out.pid


import arrow.core.nonEmptyListOf
import eu.europa.ec.eudi.pidissuer.domain.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

internal class GetPidDataFromKeyCloakTest {
    @Test
    internal fun `maps minimal data from Keycloak`() = runTest {
        val client: KeycloakClient = mock(KeycloakClient::class.java)
        given(client.getUserByUsername("user")).willReturn(
            UserRepresentation(
                "tneal", "Neal", "Tyler", mapOf(
                    Pair("birthdate", listOf("1955-04-12"))
                )
            )
        )

        val (pid, _) = checkNotNull(
            GetPidDataFromKeyCloak(
                keyCloakClient = client,
                issuerCountry = IsoCountry("GR"),
                issuingJurisdiction = "GR-I",
                clock = Clock.fixed(Instant.parse("2026-01-01T12:39:44Z"), TimeZone.UTC)
            ).invoke("user")
        )

        assertEquals(FamilyName("Neal"), pid.familyName)
        assertEquals(GivenName("Tyler"), pid.givenName)
        assertEquals(LocalDate.parse("1955-04-12", LocalDate.Formats.ISO), pid.birthDate)
        assertEquals(PlaceOfBirth(locality = City("Not known")), pid.placeOfBirth)
        assertEquals(nonEmptyListOf(Nationality("GR")), pid.nationalities)
        assertNotNull(pid.portrait)

        assertNull(pid.residentAddress)
        assertNull(pid.residentCountry)
        assertNull(pid.residentState)
        assertNull(pid.residentCity)
        assertNull(pid.residentPostalCode)
        assertNull(pid.residentStreet)
        assertNull(pid.residentHouseNumber)
        assertNull(pid.familyNameBirth)
        assertNull(pid.givenNameBirth)
        assertNull(pid.sex)
        assertNull(pid.emailAddress)
        assertNull(pid.mobilePhoneNumber)
    }
}
