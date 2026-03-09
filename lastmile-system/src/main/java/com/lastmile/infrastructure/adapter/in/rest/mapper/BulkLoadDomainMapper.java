package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.BulkLoadDto;
import com.lastmile.domain.model.BulkLoad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BulkLoadDomainMapper {
    @Mapping(target = "successRate", expression = "java(bulkLoad.getSuccessRate())")
    BulkLoadDto toDto(BulkLoad bulkLoad);
    BulkLoad toDomain(BulkLoadDto dto);
}
