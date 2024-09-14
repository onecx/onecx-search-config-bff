package org.tkit.onecx.search.config.integration.bff.rs;

import static io.quarkus.qute.Variant.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.List;

import jakarta.ws.rs.HttpMethod;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.search.config.bff.rs.controllers.SearchConfigRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.search.config.bff.rs.internal.model.*;
import gen.org.tkit.onecx.searchconfig.internal.client.model.SearchConfig;
import gen.org.tkit.onecx.searchconfig.internal.client.model.SearchConfigLoadResult;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@LogService
@TestHTTPEndpoint(SearchConfigRestController.class)
class SearchConfigRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockGetId = "MOCK_GET";
    static final String mockPostID = "MOCK_POST";
    static final String mockLoadId = "MOCK_LOAD";
    static final String mockDeleteId = "MOCK_DELETE";
    static final String mockPutId = "MOCK_PUT";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(mockGetId);
            mockServerClient.clear(mockDeleteId);
            mockServerClient.clear(mockPostID);
            mockServerClient.clear(mockLoadId);
            mockServerClient.clear(mockPutId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void deleteSearchConfigTest_shouldDeleteExistingSearchConfig() {
        String configId = "c1";
        var searchConfig = new SearchConfig();
        searchConfig.setId(configId);
        // create mock get rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(searchConfig)));

        // create mock delete rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.DELETE))
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

        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(NOT_FOUND.getStatusCode()));

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get(configId)
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteSearchConfigTest_shouldNotDeleteUnexistingSearchConfig() {
        String configId = "c1";
        // create mock get rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(NOT_FOUND.getStatusCode()));

        // create mock delete rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.DELETE))
                .withId(mockDeleteId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode()));

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", configId)
                .get("/{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode()).log().all();

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("configId", configId)
                .delete("/{configId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void loadSearchConfigInfos_shouldReturnAllSearchConfigInfos() {
        var info1 = new SearchConfigLoadResult();
        info1.setId("1");
        info1.setAdvanced(true);
        info1.setColumns(List.of("col-1", "col-2"));
        info1.setName("config-1");
        info1.setReadOnly(false);
        info1.setValues(new HashMap<>() {
            {
                put("key1", "val1");
                put("key2", "val2");
            }
        });
        var info2 = new SearchConfigLoadResult();
        info2.setId("2");
        info2.setAdvanced(false);
        info2.setColumns(List.of("col-3", "col-4"));
        info2.setName("config-2");
        info2.setReadOnly(true);
        info2.setValues(new HashMap<>() {
            {
                put("key3", "val3");
                put("key4", "val4");
            }
        });
        var searchConfigInfos = List.of(info1, info2);
        // create mock post rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/load").withMethod(HttpMethod.POST))
                .withId(mockLoadId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(searchConfigInfos)));

        var request = new GetSearchConfigInfosRequestDTO();
        request.setPage("page");
        request.setProductName("product");
        request.setAppId("appId");

        var output = given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/infos/")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetSearchConfigInfosResponseDTO.class);

        Assertions.assertEquals(output.getConfigs().size(), 2);
        Assertions.assertTrue(output.getConfigs().stream().anyMatch((item -> item.getId().equals(info1.getId()))));
        Assertions.assertTrue(output.getConfigs().stream().anyMatch((item -> item.getId().equals(info2.getId()))));
    }

    @Test
    void createSearchConfig_shouldCreateSearchConfig() {
        var createdConfig = new SearchConfig();
        createdConfig.setId("created");
        createdConfig.setName("my-new-config");
        createdConfig.setAppId("appId");
        createdConfig.setProductName("productName");
        createdConfig.setPage("page");

        mockServerClient
                .when(request().withPath("/internal/searchConfig").withMethod(HttpMethod.POST))
                .withId(mockPostID)
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(createdConfig)));

        var info1 = new SearchConfigLoadResult();
        info1.setId("1");

        var createdInfo = new SearchConfigLoadResult();
        createdInfo.setId(createdConfig.getId());

        var searchConfigInfos = List.of(info1, createdInfo);

        // create mock post rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/load").withMethod(HttpMethod.POST)).withId(mockLoadId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(searchConfigInfos)));

        var request = new CreateSearchConfigRequestDTO();
        request.setName("my-new-config");
        request.setAppId("appId");
        request.setColumns(List.of("col1"));
        request.setFieldListVersion(1);
        request.setIsAdvanced(false);
        request.setIsReadonly(false);
        request.setPage("page");
        request.setProductName("product");
        request.setValues(new HashMap<>() {
            {
                put("key", "value");
            }
        });

        var output = given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(request)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateSearchConfigResponseDTO.class);

        Assertions.assertEquals(createdConfig.getId(), output.getId());
        Assertions.assertEquals(output.getConfigs().size(), 2);
        Assertions.assertTrue(output.getConfigs().stream().anyMatch((item -> item.getId().equals(info1.getId()))));
        Assertions.assertTrue(output.getConfigs().stream().anyMatch((item -> item.getId().equals(createdConfig.getId()))));
    }

    @Test
    void createSearchConfig_shouldReturnBadRequestIfAlreadyExists() {
        var createdConfig = new SearchConfig();
        createdConfig.setId("newConfigId");
        createdConfig.setName("existing-config");

        mockServerClient.when(request().withPath("/internal/searchConfig").withMethod(HttpMethod.POST)).withId(mockPostID)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var request = new CreateSearchConfigRequestDTO();
        request.setName("existing-config");
        request.setAppId("appId");
        request.setColumns(List.of());
        request.setFieldListVersion(1);
        request.setIsAdvanced(false);
        request.setIsReadonly(false);
        request.setPage("page");
        request.setProductName("product");
        request.setValues(new HashMap<>());

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(request)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode()).log().all();
    }

    @Test
    void getSearchConfigTest_shouldReturnSearchConfig() {
        String configId = "c1";
        var searchConfig = new SearchConfig();
        searchConfig.setId(configId);
        searchConfig.setName("my-conf");
        searchConfig.setColumns(List.of("col1", "col2"));
        searchConfig.setValues(new HashMap<>() {
            {
                put("name", "Cap");
            }
        });
        searchConfig.setReadOnly(true);
        searchConfig.setAdvanced(false);
        searchConfig.setAppId("myApp");
        searchConfig.setProductName("myProduct");
        searchConfig.setPage("myPage");
        searchConfig.setModificationCount(13);
        searchConfig.setFieldListVersion(3);
        // create mock get rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(searchConfig)));

        var output = given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", configId)
                .get("/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetSearchConfigResponseDTO.class);

        var outputConfig = output.getConfig();
        Assertions.assertEquals(outputConfig.getId(), searchConfig.getId());
        Assertions.assertEquals(outputConfig.getName(), searchConfig.getName());
        Assertions.assertEquals(outputConfig.getColumns().size(), searchConfig.getColumns().size());
        Assertions.assertTrue(outputConfig.getColumns().containsAll(searchConfig.getColumns()));
        Assertions.assertEquals(outputConfig.getValues().size(), searchConfig.getValues().size());
        Assertions.assertTrue(outputConfig.getValues().entrySet().containsAll(searchConfig.getValues().entrySet()));
        Assertions.assertEquals(outputConfig.getIsReadonly(), searchConfig.getReadOnly());
        Assertions.assertEquals(outputConfig.getIsAdvanced(), searchConfig.getAdvanced());
        Assertions.assertEquals(outputConfig.getAppId(), searchConfig.getAppId());
        Assertions.assertEquals(outputConfig.getProductName(), searchConfig.getProductName());
        Assertions.assertEquals(outputConfig.getPage(), searchConfig.getPage());
        Assertions.assertEquals(outputConfig.getModificationCount(), searchConfig.getModificationCount());
        Assertions.assertEquals(outputConfig.getFieldListVersion(), searchConfig.getFieldListVersion());
    }

    @Test
    void getSearchConfigTest_shouldReturnNotFound() {
        String configId = "c1";
        var searchConfig = new SearchConfig();
        searchConfig.setId(configId);

        mockServerClient.when(request().withPath("/internal/searchConfig/" + configId).withMethod(HttpMethod.GET))
                .withId(mockGetId)
                .respond(httpRequest -> response().withStatusCode(NOT_FOUND.getStatusCode()));

        given()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get(configId)
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateSearchConfigTest_shouldReturnOk() {
        var editedConfig = new SearchConfig();
        editedConfig.setId("edited");
        editedConfig.setName("my-edited-config");
        editedConfig.setAppId("appId");
        editedConfig.setProductName("productName");
        editedConfig.setPage("page");

        mockServerClient.when(request().withPath(
                "/internal/searchConfig/" + editedConfig.getId()).withMethod(HttpMethod.PUT)).withId(mockPutId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(editedConfig)));

        var info1 = new SearchConfigLoadResult();
        info1.setId("1");

        var editedInfo = new SearchConfigLoadResult();
        editedInfo.setId(editedConfig.getId());

        var searchConfigInfos = List.of(info1, editedInfo);

        // create mock post rest endpoint
        mockServerClient.when(request().withPath("/internal/searchConfig/load").withMethod(HttpMethod.POST)).withId(mockLoadId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(searchConfigInfos)));

        var searchConfigDto = new SearchConfigDTO();
        searchConfigDto.setId(editedConfig.getId());
        searchConfigDto.setName(editedConfig.getName());
        searchConfigDto.setAppId(editedConfig.getAppId());
        searchConfigDto.setProductName(editedConfig.getProductName());
        searchConfigDto.setPage(editedConfig.getPage());
        searchConfigDto.setModificationCount(1);
        searchConfigDto.setIsAdvanced(false);
        searchConfigDto.setIsReadonly(false);
        searchConfigDto.setFieldListVersion(1);
        var request = new UpdateSearchConfigRequestDTO();
        request.setSearchConfig(searchConfigDto);

        var output = given().auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(request)
                .put(editedConfig.getId())
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UpdateSearchConfigResponseDTO.class);

        Assertions.assertTrue(output.getConfigs().stream()
                .anyMatch(info -> info.getId().equals(editedConfig.getId())));
        Assertions.assertEquals(output.getConfigs().size(), searchConfigInfos.size());
    }

    @Test
    void updateSearchConfigTest_shouldReturnBadRequestIfAlreadyExist() {
        var editedConfig = new SearchConfig();
        editedConfig.setId("id");
        editedConfig.setName("exist-with-same-appId-productName-and-page");
        editedConfig.setAppId("appId");
        editedConfig.setProductName("product");
        editedConfig.setPage("page");

        mockServerClient.when(request().withPath(
                "/internal/searchConfig/" + editedConfig.getId()).withMethod(HttpMethod.PUT)).withId(mockPutId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var searchConfigDto = new SearchConfigDTO();
        searchConfigDto.setId(editedConfig.getId());
        searchConfigDto.setName(editedConfig.getName());
        searchConfigDto.setAppId(editedConfig.getAppId());
        searchConfigDto.setProductName(editedConfig.getProductName());
        searchConfigDto.setPage(editedConfig.getPage());
        searchConfigDto.setModificationCount(1);
        searchConfigDto.setIsAdvanced(false);
        searchConfigDto.setIsReadonly(false);
        searchConfigDto.setFieldListVersion(1);
        var request = new UpdateSearchConfigRequestDTO();
        request.setSearchConfig(searchConfigDto);

        given().auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(request)
                .put(editedConfig.getId())
                .then()
                .statusCode(BAD_REQUEST.getStatusCode()).log().all();
    }
}
