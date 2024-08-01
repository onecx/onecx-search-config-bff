package org.tkit.onecx.search.config.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import jakarta.ws.rs.HttpMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.search.config.bff.rs.controllers.SearchConfigRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.searchconfig.v1.client.model.SearchConfig;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(SearchConfigRestController.class)
class SearchConfigRestControllerTest extends AbstractTest {
    KeycloakTestClient keycloakClient = new KeycloakTestClient();
    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockGetId = "MOCK_GET";
    static final String mockDeleteId = "MOCK_DELETE";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(mockGetId);
            mockServerClient.clear(mockDeleteId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void shouldDeleteExistingSearchConfig() {
        String configId = "c1";
        var searchConfig = new SearchConfig();
        searchConfig.setConfigId(configId);
        // create mock get rest endpoint
        mockServerClient.when(request().withPath("/v1/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(searchConfig)));

        // create mock delete rest endpoint
        mockServerClient.when(request().withPath("/v1/searchConfig/" + configId).withMethod(HttpMethod.DELETE))
                .withId(mockDeleteId)
                .respond(httpRequest -> response().withStatusCode(NO_CONTENT.getStatusCode()));

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", configId)
                .get("/{id}")
                .then()
                .statusCode(OK.getStatusCode()).log().all();

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("configId", configId)
                .delete("/{configId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        mockServerClient.clear(mockGetId);

        mockServerClient.when(request().withPath("/v1/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(NOT_FOUND.getStatusCode()));

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get(configId)
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}