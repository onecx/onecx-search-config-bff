package org.tkit.onecx.search.config.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.search.config.bff.rs.internal.model.*;
import gen.org.tkit.onecx.searchconfig.internal.client.model.*;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface SearchConfigMapper {
    @Mapping(target = "removeValuesItem", ignore = true)
    @Mapping(target = "removeColumnsItem", ignore = true)
    @Mapping(target = "isAdvanced", source = "advanced")
    @Mapping(target = "isReadonly", source = "readOnly")
    SearchConfigDTO map(SearchConfig searchConfig);

    @Mapping(target = "removeValuesItem", ignore = true)
    @Mapping(target = "removeColumnsItem", ignore = true)
    @Mapping(target = "isAdvanced", source = "advanced")
    @Mapping(target = "isReadonly", source = "readOnly")
    SearchConfigInfoDTO mapInfo(SearchConfigLoadResult loadResult);

    List<SearchConfigInfoDTO> mapInfoList(List<SearchConfigLoadResult> loadResult);

    @Mapping(target = "page", source = "searchConfig.page")
    @Mapping(target = "name", source = "searchConfig.name")
    @Mapping(target = "appId", source = "searchConfig.appId")
    @Mapping(target = "productName", source = "searchConfig.productName")
    @Mapping(target = "modificationCount", source = "searchConfig.modificationCount")
    @Mapping(target = "fieldListVersion", source = "searchConfig.fieldListVersion")
    @Mapping(target = "readOnly", source = "searchConfig.isReadonly")
    @Mapping(target = "advanced", source = "searchConfig.isAdvanced")
    @Mapping(target = "columns", source = "searchConfig.columns")
    @Mapping(target = "values", source = "searchConfig.values")
    UpdateSearchConfigRequest update(UpdateSearchConfigRequestDTO updateSearchConfigRequestDTO);

    // https://github.com/mapstruct/mapstruct/issues/2326#issuecomment-761817392
    default UpdateSearchConfigResponseDTO mapUpdate(List<SearchConfigLoadResult> loadResult) {
        return mapUpdate(null, loadResult);
    }

    @Mapping(target = "removeConfigsItem", ignore = true)
    @Mapping(target = "configs", source = "loadResult")
    UpdateSearchConfigResponseDTO mapUpdate(SearchConfigLoadResult dummy, List<SearchConfigLoadResult> loadResult);

    @Mapping(target = "config", source = "searchConfig")
    GetSearchConfigResponseDTO mapGet(SearchConfig searchConfig);

    SearchConfigLoadRequest getInfos(GetSearchConfigInfosRequestDTO getSearchConfigInfosRequestDTO);

    SearchConfigLoadRequest getInfos(UpdateSearchConfigRequest updateSearchConfigRequest);

    SearchConfigLoadRequest getInfos(CreateSearchConfigRequest createSearchConfigRequest);

    // https://github.com/mapstruct/mapstruct/issues/2326#issuecomment-761817392
    default GetSearchConfigInfosResponseDTO mapGetInfos(List<SearchConfigLoadResult> loadResult) {
        return mapGetInfos(null, loadResult);
    }

    @Mapping(target = "removeConfigsItem", ignore = true)
    @Mapping(target = "configs", source = "loadResult")
    GetSearchConfigInfosResponseDTO mapGetInfos(SearchConfigLoadResult dummy, List<SearchConfigLoadResult> loadResult);

    @Mapping(source = "isReadonly", target = "readOnly")
    @Mapping(source = "isAdvanced", target = "advanced")
    CreateSearchConfigRequest create(CreateSearchConfigRequestDTO configRequestDTO);

    @Mapping(target = "removeConfigsItem", ignore = true)
    @Mapping(target = "configs", source = "loadResult")
    @Mapping(target = "id", source = "id")
    CreateSearchConfigResponseDTO mapCreate(String id, List<SearchConfigLoadResult> loadResult);
}
