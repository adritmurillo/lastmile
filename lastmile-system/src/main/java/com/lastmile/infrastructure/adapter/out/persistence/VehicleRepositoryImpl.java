package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.Vehicle;
import com.lastmile.domain.port.out.VehicleRepository;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.VehiclePersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepository {
    private final VehicleJpaRepository jpaRepository;
    private final VehiclePersistenceMapper mapper;

    @Override
    public Vehicle save(Vehicle vehicle) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(vehicle)));
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
