package org.tkit.onecx.search.config.bff.rs.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.search.config.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.search.config.bff.rs.mappers.SearchConfigMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.search.config.bff.rs.internal.SearchConfigApiService;
import gen.org.tkit.onecx.search.config.bff.rs.internal.model.*;
import gen.org.tkit.onecx.searchconfig.internal.client.api.SearchConfigInternalApi;
import gen.org.tkit.onecx.searchconfig.internal.client.model.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class SearchConfigRestController implements SearchConfigApiService {
    @Inject
    @RestClient
    SearchConfigInternalApi searchConfigApi;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    SearchConfigMapper mapper;

    @Override
    public Response updateSearchConfig(String id, UpdateSearchConfigRequestDTO updateSearchConfigRequestDTO) {
        UpdateSearchConfigRequest updatedSearchConfig = mapper.update(updateSearchConfigRequestDTO);
        try (Response updateResponse = searchConfigApi.updateConfig(id, updatedSearchConfig)) {
            SearchConfigLoadRequest searchConfigLoadRequest = mapper.getInfos(updatedSearchConfig);
            try (Response findConfigsResponse = searchConfigApi.loadByProductAppAndPage(searchConfigLoadRequest)) {
                UpdateSearchConfigResponseDTO responseDTO = mapper
                        .mapUpdate(findConfigsResponse.readEntity(new GenericType<List<SearchConfigLoadResult>>() {
                        }));
                return Response.status(updateResponse.getStatus()).entity(responseDTO).build();
            }
        }
    }

    @Override
    public Response deleteSearchConfig(String id) {
        try (Response response = searchConfigApi.deleteConfig(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getSearchConfig(String id) {
        try (Response response = searchConfigApi.getConfigById(id)) {
            GetSearchConfigResponseDTO responseDTO = mapper.mapGet(response.readEntity(SearchConfig.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getSearchConfigInfos(GetSearchConfigInfosRequestDTO getSearchConfigInfosRequestDTO) {
        SearchConfigLoadRequest searchConfigLoadRequest = mapper.getInfos(getSearchConfigInfosRequestDTO);
        try (Response findSearchConfigsResponse = searchConfigApi.loadByProductAppAndPage(searchConfigLoadRequest)) {
            GetSearchConfigInfosResponseDTO responseDTO = mapper
                    .mapGetInfos(findSearchConfigsResponse.readEntity(new GenericType<List<SearchConfigLoadResult>>() {
                    }));
            return Response.status(findSearchConfigsResponse.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response createSearchConfig(CreateSearchConfigRequestDTO createSearchConfigRequestDTO) {
        CreateSearchConfigRequest createSearchConfigRequest = mapper.create(createSearchConfigRequestDTO);
        try (Response createResponse = searchConfigApi.createConfig(createSearchConfigRequest)) {
            SearchConfigDTO searchConfigDTO = mapper.map(createResponse.readEntity(SearchConfig.class));
            SearchConfigLoadRequest searchConfigLoadRequest = mapper.getInfos(createSearchConfigRequest);
            try (Response findConfigsResponse = searchConfigApi.loadByProductAppAndPage(searchConfigLoadRequest)) {
                CreateSearchConfigResponseDTO responseDTO = mapper
                        .mapCreate(searchConfigDTO.getId(),
                                findConfigsResponse.readEntity(new GenericType<List<SearchConfigLoadResult>>() {
                                }));
                return Response.status(createResponse.getStatus()).entity(responseDTO).build();
            }
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
