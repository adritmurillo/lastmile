package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.BulkLoad;
import com.lastmile.infrastructure.adapter.out.persistence.entity.BulkLoadEntity;
import com.lastmile.infrastructure.adapter.out.persistence.entity.BulkLoadErrorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BulkLoadPersistenceMapper {
    BulkLoadEntity toEntity(BulkLoad bulkLoad);
    BulkLoad toDomain(BulkLoadEntity entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bulkLoad", ignore = true)
    BulkLoadErrorEntity toErrorEntity(BulkLoad.LoadError error);
    BulkLoad.LoadError toErrorDomain(BulkLoadErrorEntity entity);
}
