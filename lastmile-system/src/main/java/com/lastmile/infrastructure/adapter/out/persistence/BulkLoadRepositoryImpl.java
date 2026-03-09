package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.BulkLoad;
import com.lastmile.domain.port.out.BulkLoadRepository;
import com.lastmile.infrastructure.adapter.out.persistence.entity.BulkLoadEntity;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.BulkLoadPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.BulkLoadJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BulkLoadRepositoryImpl implements BulkLoadRepository {
    private final BulkLoadJpaRepository jpaRepository;
    private final BulkLoadPersistenceMapper mapper;

    @Override
    public BulkLoad save(BulkLoad bulkLoad) {
        BulkLoadEntity entity = mapper.toEntity(bulkLoad);
        if(entity.getErrors() != null){
            entity.getErrors().forEach(error -> error.setBulkLoad(entity));
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<BulkLoad> findById(UUID id) {
        return jpaRepository.findByIdWithErrors(id).map(mapper :: toDomain);
    }
}
