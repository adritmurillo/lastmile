package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.ScheduledJobRun;
import com.lastmile.domain.port.out.ScheduledJobRunRepository;
import com.lastmile.infrastructure.adapter.out.persistence.entity.ScheduledJobRunEntity;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.ScheduledJobRunPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.ScheduledJobRunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScheduledJobRunRepositoryImpl implements ScheduledJobRunRepository {
    
    private final ScheduledJobRunJpaRepository jpaRepository;
    private final ScheduledJobRunPersistenceMapper mapper;
    
    @Override
    public ScheduledJobRun save(ScheduledJobRun jobRun) {
        ScheduledJobRunEntity entity = mapper.toEntity(jobRun);
        return mapper.toDomain(jpaRepository.save(entity));
    }
    
    @Override
    public Optional<ScheduledJobRun> findByJobName(String jobName) {
        return jpaRepository.findByJobName(jobName).map(mapper::toDomain);
    }
    
    @Override
    public List<ScheduledJobRun> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }
    
    @Override
    public boolean existsByJobName(String jobName) {
        return jpaRepository.existsByJobName(jobName);
    }
}
