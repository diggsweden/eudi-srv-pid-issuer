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

    @Test
    internal fun `maps all data from Keycloak`() = runTest {
        val client: KeycloakClient = mock(KeycloakClient::class.java)
        given(client.getUserByUsername("user")).willReturn(
            UserRepresentation(
                username = "tneal",
                lastName = "Neal",
                firstName = "Tyler",
                email = "tyler.neal@example.com",
                attributes = mapOf(
                    Pair("birthdate", listOf("1955-04-12")),
                    Pair("formatted", listOf("Trauner")),
                    Pair("country", listOf("AT")),
                    Pair("region", listOf("Lower Austria")),
                    Pair("locality", listOf("Gemeinde Biberbach")),
                    Pair("postal_code", listOf("3331")),
                    Pair("street", listOf("Trauner")),
                    Pair("address_house_number", listOf("101")),
                    Pair("birth_family_name", listOf("Neal")),
                    Pair("birth_given_name", listOf("Tyler")),
                    Pair("birth_place", listOf("101 Trauner")),
                    Pair("birth_country", listOf("AT")),
                    Pair("birth_state", listOf("Lower Austria")),
                    Pair("birth_city", listOf("Gemeinde Biberbach")),
                    Pair("gender", listOf("1")),
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
        assertEquals(
            PlaceOfBirth(
                IsoCountry("AT"), State("Lower Austria"), City("101 Trauner")
            ), pid.placeOfBirth
        )
        assertEquals(nonEmptyListOf(Nationality("GR")), pid.nationalities)
        assertNotNull(pid.portrait)
        assertEquals("Trauner", pid.residentAddress)
        assertEquals(IsoCountry("AT"), pid.residentCountry)
        assertEquals(State("Lower Austria"), pid.residentState)
        assertEquals(City("Gemeinde Biberbach"), pid.residentCity)
        assertEquals(PostalCode("3331"), pid.residentPostalCode)
        assertEquals(Street("Trauner"), pid.residentStreet)
        assertEquals("101", pid.residentHouseNumber)
        assertEquals(FamilyName("Neal"), pid.familyNameBirth)
        assertEquals(GivenName("Tyler"), pid.givenNameBirth)
        assertEquals(IsoGender(1u), pid.sex)
        assertEquals("tyler.neal@example.com", pid.emailAddress)
        assertEquals(null, pid.mobilePhoneNumber)
    }
}
