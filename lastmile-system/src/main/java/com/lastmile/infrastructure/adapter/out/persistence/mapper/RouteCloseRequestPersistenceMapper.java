package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.CloseRequestStatus;
import com.lastmile.domain.model.RouteCloseReason;
import com.lastmile.domain.model.RouteCloseRequest;
import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteCloseRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteCloseRequestPersistenceMapper {
    
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "courier", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reason", source = "reason", qualifiedByName = "reasonToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    RouteCloseRequestEntity toEntity(RouteCloseRequest domain);
    
    @Mapping(target = "routeId", expression = "java(entity.getRoute() != null ? entity.getRoute().getId() : null)")
    @Mapping(target = "courierId", expression = "java(entity.getCourier() != null ? entity.getCourier().getId() : null)")
    @Mapping(target = "reviewedBy", expression = "java(entity.getReviewedBy() != null ? entity.getReviewedBy().getId() : null)")
    @Mapping(target = "reason", source = "reason", qualifiedByName = "stringToReason")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "courierName", expression = "java(entity.getCourier() != null ? entity.getCourier().getFirstName() + \" \" + entity.getCourier().getLastName() : null)")
    @Mapping(target = "routeCode", expression = "java(entity.getRoute() != null ? entity.getRoute().getDate().toString() + \"-\" + entity.getCourier().getFirstName() : null)")
    @Mapping(target = "pendingStopsCount", ignore = true)
    RouteCloseRequest toDomain(RouteCloseRequestEntity entity);
    
    List<RouteCloseRequest> toDomainList(List<RouteCloseRequestEntity> entities);
    
    @Named("reasonToString")
    default String reasonToString(RouteCloseReason reason) {
        return reason != null ? reason.name() : null;
    }
    
    @Named("stringToReason")
    default RouteCloseReason stringToReason(String reason) {
        return reason != null ? RouteCloseReason.valueOf(reason) : null;
    }
    
    @Named("statusToString")
    default String statusToString(CloseRequestStatus status) {
        return status != null ? status.name() : null;
    }
    
    @Named("stringToStatus")
    default CloseRequestStatus stringToStatus(String status) {
        return status != null ? CloseRequestStatus.valueOf(status) : null;
    }
}
