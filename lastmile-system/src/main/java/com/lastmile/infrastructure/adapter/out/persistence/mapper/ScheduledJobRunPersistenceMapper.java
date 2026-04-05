package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.JobRunStatus;
import com.lastmile.domain.model.ScheduledJobRun;
import com.lastmile.infrastructure.adapter.out.persistence.entity.ScheduledJobRunEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduledJobRunPersistenceMapper {
    
    @Mapping(target = "lastStatus", source = "lastStatus", qualifiedByName = "statusToString")
    ScheduledJobRunEntity toEntity(ScheduledJobRun domain);
    
    @Mapping(target = "lastStatus", source = "lastStatus", qualifiedByName = "stringToStatus")
    ScheduledJobRun toDomain(ScheduledJobRunEntity entity);
    
    List<ScheduledJobRun> toDomainList(List<ScheduledJobRunEntity> entities);
    
    @Named("statusToString")
    default String statusToString(JobRunStatus status) {
        return status != null ? status.name() : null;
    }
    
    @Named("stringToStatus")
    default JobRunStatus stringToStatus(String status) {
        return status != null ? JobRunStatus.valueOf(status) : null;
    }
}
