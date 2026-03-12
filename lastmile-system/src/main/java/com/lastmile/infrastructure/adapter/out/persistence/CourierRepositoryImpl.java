package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.Courier;
import com.lastmile.domain.port.out.CourierRepository;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.CourierPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.CourierJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {
    private final CourierJpaRepository jpaRepository;
    private final CourierPersistenceMapper mapper;

    @Override
    public Courier save(Courier courier) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(courier)));
    }

    @Override
    public Optional<Courier> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper :: toDomain);
    }

    @Override
    public Optional<Courier> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper :: toDomain);
    }

    @Override
    public List<Courier> findAvailableToday() {
        return jpaRepository.findAvailableCouriersWithVehicle()
                .stream()
                .map(mapper :: toDomain)
                .toList();
    }

    @Override
    public List<Courier> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper :: toDomain)
                .toList();
    }
}
