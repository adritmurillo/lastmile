package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.BulkLoadDto;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.BulkLoadResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BulkLoadRestMapper {
    BulkLoadResponse toResponse(BulkLoadDto dto);
}
